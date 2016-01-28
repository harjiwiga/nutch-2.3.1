/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.parse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.StringUtils;
import org.apache.nutch.crawl.CrawlStatus;
import org.apache.nutch.crawl.Signature;
import org.apache.nutch.crawl.SignatureFactory;
import org.apache.nutch.fetcher.DomainConstants;
import org.apache.nutch.fetcher.FetcherJob;
import org.apache.nutch.fetcher.JsoupUtil;
import org.apache.nutch.net.URLFilterException;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.storage.Mark;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.TableUtil;
import org.apache.nutch.util.URLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

// Commons Logging imports

/**
 * A Utility class containing methods to simply perform parsing utilities such
 * as iterating through a preferred list of {@link Parser}s to obtain
 * {@link Parse} objects.
 *
 * @author mattmann
 * @author J&eacute;r&ocirc;me Charron
 * @author S&eacute;bastien Le Callonnec
 */
public class ParseUtil extends Configured {

	/* our log stream */
	public static final Logger LOG = LoggerFactory.getLogger(ParseUtil.class);

	private static final int DEFAULT_MAX_PARSE_TIME = 30;

	private Configuration conf;
	private Signature sig;
	private URLFilters filters;
	private URLNormalizers normalizers;
	private int maxOutlinks;
	private boolean ignoreExternalLinks;
	private ParserFactory parserFactory;
	/** Parser timeout set to 30 sec by default. Set -1 to deactivate **/
	private int maxParseTime;
	private ExecutorService executorService;

	/**
	 *
	 * @param conf
	 */
	public ParseUtil(Configuration conf) {
		super(conf);
		setConf(conf);
	}

	@Override
	public Configuration getConf() {
		return conf;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
		parserFactory = new ParserFactory(conf);
		maxParseTime = conf.getInt("parser.timeout", DEFAULT_MAX_PARSE_TIME);
		sig = SignatureFactory.getSignature(conf);
		filters = new URLFilters(conf);
		normalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_OUTLINK);
		int maxOutlinksPerPage = conf.getInt("db.max.outlinks.per.page", 100);
		maxOutlinks = (maxOutlinksPerPage < 0) ? Integer.MAX_VALUE : maxOutlinksPerPage;
		ignoreExternalLinks = conf.getBoolean("db.ignore.external.links", false);
		executorService = Executors
				.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("parse-%d").setDaemon(true).build());
	}

	/**
	 * Performs a parse by iterating through a List of preferred {@link Parser}s
	 * until a successful parse is performed and a {@link Parse} object is
	 * returned. If the parse is unsuccessful, a message is logged to the
	 * <code>WARNING</code> level, and an empty parse is returned.
	 *
	 * @throws ParserNotFound
	 *             If there is no suitable parser found.
	 * @throws ParseException
	 *             If there is an error parsing.
	 */
	public Parse parse(String url, WebPage page) throws ParserNotFound, ParseException {
		Parser[] parsers = null;

		String contentType = TableUtil.toString(page.getContentType());

		parsers = this.parserFactory.getParsers(contentType, url);

		for (int i = 0; i < parsers.length; i++) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Parsing [" + url + "] with [" + parsers[i] + "]");
			}
			Parse parse = null;

			if (maxParseTime != -1)
				parse = runParser(parsers[i], url, page);
			else
				parse = parsers[i].getParse(url, page);

			if (parse != null && ParseStatusUtils.isSuccess(parse.getParseStatus())) {
				return parse;
			}
		}

		LOG.warn("Unable to successfully parse content " + url + " of type " + contentType);
		return ParseStatusUtils.getEmptyParse(new ParseException("Unable to successfully parse content"), null);
	}

	private Parse runParser(Parser p, String url, WebPage page) {
		ParseCallable pc = new ParseCallable(p, page, url);
		Future<Parse> task = executorService.submit(pc);
		Parse res = null;
		try {
			res = task.get(maxParseTime, TimeUnit.SECONDS);
		} catch (Exception e) {
			LOG.warn("Error parsing " + url, e);
			task.cancel(true);
		} finally {
			pc = null;
		}
		return res;
	}

	/**
	 * Parses given web page and stores parsed content within page. Puts a
	 * meta-redirect to outlinks.
	 * 
	 * @param key
	 * @param page
	 */
	public void process(String key, WebPage page) {
		String url = TableUtil.unreverseUrl(key);
		byte status = page.getStatus().byteValue();
		if (status != CrawlStatus.STATUS_FETCHED) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Skipping " + url + " as status is: " + CrawlStatus.getName(status));
			}
			return;
		}

		Parse parse;
		try {
			parse = parse(url, page);
		} catch (ParserNotFound e) {
			// do not print stacktrace for the fact that some types are not
			// mapped.
			LOG.warn("No suitable parser found: " + e.getMessage());
			return;
		} catch (final Exception e) {
			LOG.warn("Error parsing: " + url + ": " + StringUtils.stringifyException(e));
			return;
		}

		if (parse == null) {
			return;
		}

		org.apache.nutch.storage.ParseStatus pstatus = parse.getParseStatus();
		page.setParseStatus(pstatus);
		if (ParseStatusUtils.isSuccess(pstatus)) {
			if (pstatus.getMinorCode() == ParseStatusCodes.SUCCESS_REDIRECT) {
				String newUrl = ParseStatusUtils.getMessage(pstatus);
				int refreshTime = Integer.parseInt(ParseStatusUtils.getArg(pstatus, 1));
				try {
					newUrl = normalizers.normalize(newUrl, URLNormalizers.SCOPE_FETCHER);
					if (newUrl == null) {
						LOG.warn("redirect normalized to null " + url);
						return;
					}
					try {
						newUrl = filters.filter(newUrl);
					} catch (URLFilterException e) {
						return;
					}
					if (newUrl == null) {
						LOG.warn("redirect filtered to null " + url);
						return;
					}
				} catch (MalformedURLException e) {
					LOG.warn("malformed url exception parsing redirect " + url);
					return;
				}
				page.getOutlinks().put(new Utf8(newUrl), new Utf8());
				page.getMetadata().put(FetcherJob.REDIRECT_DISCOVERED, TableUtil.YES_VAL);
				if (newUrl == null || newUrl.equals(url)) {
					String reprUrl = URLUtil.chooseRepr(url, newUrl, refreshTime < FetcherJob.PERM_REFRESH_TIME);
					if (reprUrl == null) {
						LOG.warn("reprUrl==null for " + url);
						return;
					} else {
						page.setReprUrl(new Utf8(reprUrl));
					}
				}
			} else {
				page.setText(new Utf8(parse.getText()));
				page.setTitle(new Utf8(parse.getTitle()));
				ByteBuffer prevSig = page.getSignature();
				if (prevSig != null) {
					page.setPrevSignature(prevSig);
				}
				final byte[] signature = sig.calculate(page);
				page.setSignature(ByteBuffer.wrap(signature));
				if (page.getOutlinks() != null) {
					page.getOutlinks().clear();
				}
				final Outlink[] outlinks = parse.getOutlinks();
				int outlinksToStore = Math.min(maxOutlinks, outlinks.length);
				String fromHost;
				if (ignoreExternalLinks) {
					try {
						fromHost = new URL(url).getHost().toLowerCase();
					} catch (final MalformedURLException e) {
						fromHost = null;
					}
				} else {
					fromHost = null;
				}
				int validCount = 0;

				for (int i = 0; validCount < outlinksToStore && i < outlinks.length; i++) {
					String toUrl = outlinks[i].getToUrl();
					try {
						toUrl = normalizers.normalize(toUrl, URLNormalizers.SCOPE_OUTLINK);
						toUrl = filters.filter(toUrl);
					} catch (MalformedURLException e2) {
						continue;
					} catch (URLFilterException e) {
						continue;
					}
					if (toUrl == null) {
						continue;
					}
					Utf8 utf8ToUrl = new Utf8(toUrl);
					if (page.getOutlinks().get(utf8ToUrl) != null) {
						// skip duplicate outlinks
						continue;
					}

					String toHost;
					if (ignoreExternalLinks) {
						try {
							toHost = new URL(toUrl).getHost().toLowerCase();
						} catch (final MalformedURLException e) {
							toHost = null;
						}
						if (toHost == null || !toHost.equals(fromHost)) { // external
																			// links
							continue; // skip it
						}
					}
					validCount++;
					page.getOutlinks().put(utf8ToUrl, new Utf8(outlinks[i].getAnchor()));
				}

				Utf8 fetchMark = Mark.FETCH_MARK.checkMark(page);
				if (fetchMark != null) {
					Mark.PARSE_MARK.putMark(page, fetchMark);
				}
			}
		}
	}

	public void jsoup(Content content) {
		// jsoup
		if (content != null) {
			Document doc = Jsoup.parse(Bytes.toStringBinary(content.getContent()));
			if (doc != null) {
				URL url = null;
				try {
					url = new URL(content.getBaseUrl());
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String domainName = getDomainName(url);
				domainName = domainName.substring(0, domainName.lastIndexOf('.'));
				int index = domainName.lastIndexOf('.');
				if (index > 0)
					domainName = domainName.substring(0, domainName.lastIndexOf('.'));
				/*
				 * InetAddress giriAddress = null; try { giriAddress =
				 * java.net.InetAddress.getByName(url.getHost()); } catch
				 * (UnknownHostException e1) { // TODO Auto-generated catch
				 * block e1.printStackTrace(); } String address =
				 * giriAddress.getHostAddress(); String countryCode =
				 * fetchUrl("http://api.wipmania.com/" + address);
				 */
				   
				String homeDir = System.getProperty("user.home");
				String separator = System.getProperty("file.separator");
				if ((domainName).equals(DomainConstants.TOKOPEDIA)) {
					String item = !doc.select("h1 a").text().isEmpty() ? doc.select("h1 a").text()
							: doc.select("#breadcrumb-container > ul > li.active > h2").text();
					String category = doc.select("h2 a").size() > 0 ? doc.select("h2 a").get(0).text() : null;
					String subcategory = doc.select("#breadcrumb-container > ul > li:nth-child(5) > h2 > a").text();
					String subsubcategory = doc.select("#breadcrumb-container > ul > li:nth-child(7) > h2 > a").text();
					// String delims = "[ ]+";
					// String[] tokens = item.split(delims);
					// String brand = doc.select("h2 a").size() > 2 ?
					// doc.select("h2
					// a").get(2).text()
					// : (tokens.length > 0 ? tokens[0] : "");
					// System.out.println("brand: " + brand);
					System.out.println("item: " + item);
					System.out.println("price: " + doc.select("span[itemprop=price]").text());
					System.out.println("low price: " + doc.select("span[itemprop=lowprice]").text());
					System.out.println("high price: " + doc.select("span[itemprop=highprice]").text());
					System.out.println("description: " + doc.select("p[itemprop=description]").text());

					if (category != null && subcategory != null)
						if (item != null && !doc.select("span[itemprop=price]").text().isEmpty()) {

							FileWriter file = null;
							try {
								Date date = new Date();
								SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy hh:mm:ss");
								String out = new SimpleDateFormat("yyyyMMddhhmmss'.json'").format(date);
								/*
								 * String osName =
								 * System.getProperty("os.name");
								 * if(osName.startsWith("Windows")) { String[]
								 * illegalWindowsCharArray = {"/", "\\", ":", "*
								 * ", " ? ", " \"", "<", ">", "|"}; for(int i =
								 * 0; i < illegalWindowsCharArray.length; i++) {
								 * if(fileName.contains(illegalWindowsCharArray[
								 * i])) {
								 * fileName.replace(illegalWindowsCharArray[i],
								 * ""); } } }
								 */
								// String fileName = item +"-"+out;
								// fileName =
								// fileName.replaceAll("[^a-zA-Z0-9.-]",
								// "_");

								item = JsoupUtil.generateItemName(url.getFile());
								System.out.println("item domain " + item);
								index = item.indexOf('-');
								String brand;
								if (index > 0)
									brand = item.substring(0, item.indexOf('-'));
								else
									brand = item;

								System.out.println("brand  domain " + brand);
								Elements scriptElements = doc.getElementsByTag("script");
								boolean done = Boolean.FALSE;
								String id8digit = "";
								for (Element element : scriptElements) {
									for (DataNode node : element.dataNodes()) {
										if (node.getWholeData().contains("var product =")) {
											String[] result = node.getWholeData().split(" ");
											if (result[1].equals("product")) {
												id8digit = result[6].split(",")[0].replace("'", "").substring(1);
												done = Boolean.TRUE;
											}
										}
										if (done)
											break;
									}
									if (done)
										break;
								}
								String countryCode = "id";
								String path = homeDir + separator +"data"+ separator +"crawlers"+ separator + countryCode + separator + domainName + separator;
								File dir = new File(path);
								if (!dir.exists())
									dir.mkdirs();
								String fileName = category.replaceAll("[^a-zA-Z0-9.-]", "") + "_" + domainName + "_"
										+ countryCode + "_" + id8digit + "_" + out;

								file = new FileWriter(path + fileName);
								JsonGenerator jsonGenerator = new JsonFactory().createGenerator(file);
								// for pretty printing
								jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());

								jsonGenerator.writeStartObject(); // start root
																	// object
								jsonGenerator.writeStringField("site", url.getHost());
								jsonGenerator.writeStringField("country", countryCode);
								jsonGenerator.writeStringField("date_crawling", sdf.format(date));
								jsonGenerator.writeStringField("brand", brand);
								jsonGenerator.writeStringField("item", item);
								jsonGenerator.writeStringField("category", category);
								jsonGenerator.writeStringField("subcategory", subcategory);
								jsonGenerator.writeStringField("subsubcategory", subsubcategory);
								jsonGenerator.writeStringField("price",
										doc.select("span[itemprop=price]").text().isEmpty()
												? (doc.select("span[itemprop=highprice]").text().isEmpty()
														? doc.select("span[itemprop=lowprice]").text()
														: doc.select("span[itemprop=lowprice]").text() + " - "
																+ doc.select("span[itemprop=highprice]").text())
												: doc.select("span[itemprop=price]").text());
								jsonGenerator.writeStringField("description",
										doc.select("p[itemprop=description]").text());
								jsonGenerator.writeStringField("isdelivery",
										doc.select("div div div div div div li div small").size() > 0
												? doc.select("div div div div div div li div small").get(0).text()
												: "");
								jsonGenerator.writeStringField("url", content.getBaseUrl().toString());

								jsonGenerator.writeObjectFieldStart("instalment");
								jsonGenerator.writeStringField("instalpayment", doc.select("#trigger-instalpayment > small").text() );
								jsonGenerator.writeEndObject();
							
								jsonGenerator.writeObjectFieldStart("information");
								jsonGenerator.writeStringField("seen",
										doc.select("div div div dl dd.pull-left.m-0").size() > 0
												? doc.select("div div div dl dd.pull-left.m-0").get(0).text() : "");
								jsonGenerator.writeStringField("weight",
										doc.select("div div div dl dd.pull-left.m-0").size() > 1
												? doc.select("div div div dl dd.pull-left.m-0").get(1).text() : "");
								jsonGenerator.writeStringField("sold",
										doc.select("div div div dl dd.pull-left.m-0").size() > 2
												? doc.select("div div div dl dd.pull-left.m-0").get(2).text() : "");
								jsonGenerator.writeStringField("insurance",
										doc.select("div div div dl dd.pull-left.m-0").size() > 3
												? doc.select("div div div dl dd.pull-left.m-0").get(3).text() : "");
								jsonGenerator.writeStringField("condition",
										doc.select("div div div dl dd.pull-left.m-0").size() > 4
												? doc.select("div div div dl dd.pull-left.m-0").get(4).text() : "");
								jsonGenerator.writeStringField("min_order",
										doc.select("div div div dl dd.pull-left.m-0").size() > 5
												? doc.select("div div div dl dd.pull-left.m-0").get(5).text() : "");
								jsonGenerator.writeEndObject();

								jsonGenerator.writeObjectFieldStart("reviews");
								jsonGenerator.writeStringField("quality_avg",
										doc.select("div#review-summary div div div div div table tbody tr").size() > 0
												? doc.select("div#review-summary div div div div div table tbody tr")
														.get(0).text()
												: "");
								jsonGenerator.writeEndObject();

								jsonGenerator.writeObjectFieldStart("comments");
								jsonGenerator.writeStringField("username", doc.select("a[itemprop=author").text());
								jsonGenerator.writeStringField("comment",
										doc.select("span[itemprop=reviewBody]").text());
								jsonGenerator.writeStringField("quality",
										doc.select("i[class=rating-star5 mt-3]").size() > 0
												? doc.select("i[class=rating-star5 mt-3]").get(0).attr("title") : "");
								jsonGenerator.writeStringField("accuracy",
										doc.select("i[class=rating-star5 mt-3]").size() > 1
												? doc.select("i[class=rating-star5 mt-3]").get(1).attr("title") : "");
								jsonGenerator.writeStringField("date",
										doc.select(
												"#review-container > li > div > div.list-box-comment > div.list-box-top > div > small > i")
												.text());
								jsonGenerator.writeEndObject();

								jsonGenerator.writeEndObject(); // closing root
																// object

								jsonGenerator.flush();
								jsonGenerator.close();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
				} else if ((domainName).equals(DomainConstants.LAZADA)) {
					try {
						doc = Jsoup.connect(content.getBaseUrl()).timeout(60000).maxBodySize(0)
								.header("Accept-Encoding", "gzip, deflate")
								.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0").get();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

					String item = url.getFile();
					String category = doc
							.select("body > div.header > div.header__bottom > div.header__bottom__menu > div > div.header__breadcrumb > div.header__breadcrumb__wrapper > ul > li:nth-child(1) > span > a")
							.text();
					String subcategory = doc
							.select("body > div.header > div.header__bottom > div.header__bottom__menu > div > div.header__breadcrumb > div.header__breadcrumb__wrapper > ul > li:nth-child(2) > span > a")
							.text();
					String subsubcategory = doc
							.select("body > div.header > div.header__bottom > div.header__bottom__menu > div > div.header__breadcrumb > div.header__breadcrumb__wrapper > ul > li:nth-child(3) > span > a")
							.text();
					String currency = doc.select("#special_currency_box").text();
					String price = currency + " " + doc.select("#special_price_box").text();

					if (!category.isEmpty() && !subcategory.isEmpty() && !item.isEmpty() && !price.isEmpty()
							&& item.indexOf('.') > 0) {
						System.out.println("item: " + item);
						System.out.println("price: " + doc.select("span[itemprop=price]").text());
						System.out.println("low price: " + doc.select("span[itemprop=lowprice]").text());
						System.out.println("high price: " + doc.select("span[itemprop=highprice]").text());
						System.out.println("description: " + doc.select("p[itemprop=description]").text());
						item = item.substring(1, item.indexOf('.'));
						String id8digit = item.substring(item.lastIndexOf("-") + 1, item.length());
						item = item.substring(0, item.lastIndexOf('-'));
						FileWriter file = null;
						try {
							Date date = new Date();
							SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy hh:mm:ss");
							String out = new SimpleDateFormat("yyyyMMddhhmmss'.json'").format(date);
							String discount = doc.select("#special_price_area").text();
							String brand = doc.select("#prod_brand > div:nth-child(1) > a > span").text();

							String countryCode = url.getHost().substring(url.getHost().lastIndexOf(".")+1, url.getHost().length());
							String path = homeDir + separator +"data"+ separator +"crawlers"+ separator + countryCode + separator + domainName + separator;
							File dir = new File(path);
							if (!dir.exists())
								dir.mkdirs();
							String fileName = category.replaceAll("[^a-zA-Z0-9.-]", "") + "_" + domainName + "_"
									+ countryCode + "_" + id8digit + "_" + out;

							file = new FileWriter(path + fileName);
							JsonGenerator jsonGenerator = new JsonFactory().createGenerator(file);
							// for pretty printing
							jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());

							jsonGenerator.writeStartObject(); // start root
																// object
							jsonGenerator.writeStringField("site", url.getHost());
							jsonGenerator.writeStringField("country", countryCode);
							jsonGenerator.writeStringField("date_crawling", sdf.format(date));
							jsonGenerator.writeStringField("brand", brand);
							jsonGenerator.writeStringField("item", item);
							jsonGenerator.writeStringField("category", category);
							jsonGenerator.writeStringField("subcategory", subcategory);
							jsonGenerator.writeStringField("subsubcategory", subsubcategory);
							jsonGenerator.writeStringField("price", price);
							jsonGenerator.writeStringField("price_description", discount);
							jsonGenerator.writeStringField("description",
									doc.select("div[itemprop=description]").text());
							jsonGenerator.writeStringField("seller_promotion",
									doc.select(
											"#prod_content_wrapper > div.prod_l_content > div.seller__promotion__detail > span.text > span.description-details-full-text > ul > li")
											.text());
							jsonGenerator.writeStringField("url", content.getBaseUrl().toString());

							jsonGenerator.writeObjectFieldStart("product_buyer_protection");
							jsonGenerator.writeStringField("seller",
									doc.select(
											"#prod_content_wrapper > div.prod_r_content > div.product-buyer-protection > div > div > div > a")
											.text().isEmpty() ? doc
													.select("#prod_content_wrapper > div.prod_r_content > div.product-buyer-protection > div > div > div")
													.text()
													: doc.select(
															"#prod_content_wrapper > div.prod_r_content > div.product-buyer-protection > div > div > div > a")
															.text());
							jsonGenerator.writeStringField("delivery_option",
									doc.select(
											"#deliveryType > div.prod_cta.delivery-types > div.delivery-types__data.delivery-types__data-default > div > div > div.product__promotion__item__content > div.product__promotion__item__text")
											.text());							
							jsonGenerator.writeStringField("delivery_option_time",
									doc.select(
											"#deliveryType > div.prod_cta.delivery-types > div.delivery-types__data.delivery-types__data-default > div > div > div.product__promotion__item__content > div.product__promotion__item__time")
											.text());							
							jsonGenerator.writeStringField("payment_method",
									doc.select(
											"#deliveryType > div.product__promotion > div > div.product__promotion__item__content > span")
											.text());
							jsonGenerator.writeStringField("detail",
									doc.select(
											"#prod_content_wrapper > div.prod_r_content > div.product-buyer-protection > a > div.product-buyer-protection__details.product-buyer-protection__details_language_id > div.product-buyer-protection__details__item.product-buyer-protection__details__item_no_padding")
											.text());
							jsonGenerator.writeEndObject();

							Element tableSupplier = doc.select("table.product-multisource__sources__table").first();
							if (tableSupplier != null) {
								jsonGenerator.writeObjectFieldStart("supplier");
								Element tbody = tableSupplier.select("tbody").first();
								String[] key = null;
								Iterator<Element> tbRows = tbody
										.select("tr.product-multisource__sources__table__row.product-multisource__sources__table__heading")
										.iterator();
								while (tbRows.hasNext()) {
									Iterator<Element> tbCols = tbRows.next()
											.select("td.product-multisource__sources__table__cell.product-multisource__sources__table__heading__item")
											.iterator();
									List<Element> tdList = Lists.newArrayList(tbCols);
									key = new String[tdList.size()];
									for (int i = 0; i < key.length; i++)
										key[i] = tdList.get(i).text();
								}
								tbRows = tbody
										.select("tr.product-multisource__sources__table__row.product-multisource__sources__table__source.product-multisource__source ")
										.iterator();
								while (tbRows.hasNext()) {
									jsonGenerator.writeStartArray();
									// Iterator<Element> tbCols =
									// tbRows.next().select("td.product-multisource__sources__table__cell.product-multisource__sources__table__source__seller").iterator();
									Iterator<Element> tbCols = tbRows.next().select("td").iterator();
									while (tbCols.hasNext()) {
										Iterator<Element> tbCols2 = tbCols.next()
												.select("td.product-multisource__sources__table__cell.product-multisource__sources__table__source__seller")
												.iterator();
										while (tbCols2.hasNext()) 
											jsonGenerator.writeStringField(key[0], tbCols2.next().text());										
										tbCols2 = tbCols.next()
												.select("td.product-multisource__sources__table__cell.product-multisource__sources__table__source__delivery")
												.iterator();
										while (tbCols2.hasNext()) 
											jsonGenerator.writeStringField(key[1], tbCols2.next().text());										
										
										tbCols2 = tbCols.next()
												.select("td.product-multisource__sources__table__cell.product-multisource__sources__table__source__info")
												.iterator();
										while (tbCols2.hasNext()) 
											jsonGenerator.writeStringField(key[2], tbCols2.next().text());										
										tbCols2 = tbCols.next()
												.select("td.product-multisource__sources__table__cell.product-multisource__sources__table__source__price")
												.iterator();
										while (tbCols2.hasNext()) 
											jsonGenerator.writeStringField(key[3], tbCols2.next().text());																				
									}
									jsonGenerator.writeEndArray();
								}
								jsonGenerator.writeEndObject();
							}

							jsonGenerator.writeStringField("product_description",
									doc.select("#productDetails > div").text());

							jsonGenerator.writeObjectFieldStart("product_spec");
							jsonGenerator.writeStringField("check_inbox",
									doc.select(
											"#prd-detail-page > div > div.prd-detail-bottom-wrapper > div.l-main.prd-detail-wrapper > div:nth-child(2) > div.product-description__inbox.toclear")
											.text());
							Element tableSpec = doc.select("table.specification-table").first();
							if (tableSpec != null) {
								jsonGenerator.writeObjectFieldStart("spec");
								Iterator<Element> tbRows = tableSpec.select("tr").iterator();
								while (tbRows.hasNext()) {
									Iterator<Element> tbCols = tbRows.next().select("td").iterator();
									while (tbCols.hasNext()) {
										String key = tbCols.next().text();
										while (tbCols.hasNext()) {
											jsonGenerator.writeStringField(key, tbCols.next().text());
										}
									}
								}
								jsonGenerator.writeEndObject();
							}

							jsonGenerator.writeObjectFieldStart("reviews");
							jsonGenerator.writeStringField("rating",
									doc.select(
											"#js_append_rating_service_message > div.ui-box-lightBg-padded.js_rat_statistics_wrapper.ratingStats > div.ratingStarsResult > span")
											.text());
							Elements ul = doc.select("ul.ratingBarList");
							Elements li = ul.select("li.ratingBarListItem"); // select
																				// all
																				// li
																				// from
																				// ul
							for (int i = 0; i < li.size(); i++) {
								String rating = li.get(i).select("span.ratingBarLabel").text();
								jsonGenerator.writeStringField(rating, li.get(i).select("span.ratingBarCount").text());
							}
							jsonGenerator.writeEndObject();

							jsonGenerator.writeObjectFieldStart("comments");
							ul = doc.select("ul.ratRev_reviewList");
							li = ul.select("li.ratRev_reviewListRow"); // select
																		// all
																		// li
																		// from
																		// ul
							for (int i = 0; i < li.size(); i++) {
								Elements div = li.get(i).select("div.ratRev_revDetails");
								jsonGenerator.writeStartArray();
								for (int j = 0; j < div.size(); j++) {
									Elements span = div.get(j).select("span[itemprop=reviewRating]");
									for (int k = 0; k < span.size(); k++) {
										Elements ul1 = span.get(k).select("ul[itemprop=ratingValue]");
										jsonGenerator.writeStringField("rating", ul1.attr("content"));
									}
									span = div.get(j).select("span.ratRev_revTitle");
									if (span.size() > 0)
										jsonGenerator.writeStringField("title", span.text());
									span = div.get(j).select("meta[itemprop=datePublished]");
									if (span.size() > 0)
										jsonGenerator.writeStringField("date", span.attr("content"));
								}
								div = li.get(i).select("div.ratRev_revDetail");
								for (int j = 0; j < div.size(); j++) {
									jsonGenerator.writeStringField("comment",
											div.get(j).select("div[itemprop=description]").text());
								}
								div = li.get(i).select("div.ratRev-revAuthor");
								for (int j = 0; j < div.size(); j++) {
									jsonGenerator.writeStringField("badge", div.get(j)
											.select("span.ratRev-revBadge.ratRev_revDetailsItm_badge").text());
									jsonGenerator.writeStringField("username",
											div.get(j).select("span[itemprop=author]").text());
								}
								jsonGenerator.writeEndArray();
							}
							jsonGenerator.writeEndObject();

							jsonGenerator.writeEndObject(); // closing root
															// object

							jsonGenerator.flush();
							jsonGenerator.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				} else if ((domainName).equals(DomainConstants.BUKALAPAK)) {
					String item = doc.select("#breadcrumb > ol > li:nth-child(4) > span").text();
					String category = doc.select("#breadcrumb > ol > li:nth-child(2) > a").text();
					String subcategory = doc.select("#breadcrumb > ol > li:nth-child(3) > a").text();
					String price = doc.select("span[itemprop=price]").text();

					if (!category.isEmpty() && !subcategory.isEmpty() && !item.isEmpty() && !price.isEmpty()) {
						System.out.println("item: " + item);
						System.out.println("price: " + doc.select("span[itemprop=price]").text());
						System.out.println("low price: " + doc.select("span[itemprop=lowprice]").text());
						System.out.println("high price: " + doc.select("span[itemprop=highprice]").text());
						System.out.println("description: " + doc.select("p[itemprop=description]").text());
						Element productInput = doc.select("input[name=item[product_id]]").first();
						String id8digit = productInput.attr("value");
						FileWriter file = null;
						try {
							Date date = new Date();
							SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy hh:mm:ss");
							String out = new SimpleDateFormat("yyyyMMddhhmmss'.json'").format(date);
							String discount = doc.select("body > div.layout > div.container > section.clearfix > div > div.product-detailed-essential.primary-content-segment.primary-content-segment--padded > div.product-detailed-discount-badge.product-detailed-discount-badge--active > span.product-discount-percentage-amount").text()+"%";

							String countryCode = url.getHost().substring(url.getHost().lastIndexOf(".")+1, url.getHost().length());
							String path = homeDir + separator +"data"+ separator +"crawlers"+ separator + countryCode + separator + domainName + separator;
							File dir = new File(path);
							if (!dir.exists())
								dir.mkdirs();
							String fileName = category.replaceAll("[^a-zA-Z0-9.-]", "") + "_" + domainName + "_"
									+ countryCode + "_" + id8digit + "_" + out;

							file = new FileWriter(path + fileName);
							JsonGenerator jsonGenerator = new JsonFactory().createGenerator(file);
							// for pretty printing
							jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());

							jsonGenerator.writeStartObject(); // start root
																// object
							jsonGenerator.writeStringField("site", url.getHost());
							jsonGenerator.writeStringField("country", countryCode);
							jsonGenerator.writeStringField("date_crawling", sdf.format(date));
							jsonGenerator.writeStringField("item", item);
							jsonGenerator.writeStringField("category", category);
							jsonGenerator.writeStringField("subcategory", subcategory);
							jsonGenerator.writeStringField("price", price);
							jsonGenerator.writeStringField("price_original", doc.select("body > div.layout > div.container > section.clearfix > div > div.product-detailed-essential.primary-content-segment.primary-content-segment--padded > div.product-detailed-price > span.product-detailed-price__original").text());
							jsonGenerator.writeStringField("discount", discount);
							jsonGenerator.writeStringField("stock", doc.select("body > div.layout > div.container > section.clearfix > div > div.product-detailed-essential.primary-content-segment.primary-content-segment--padded > div.product-detailed-actions > div > form > div.product-buy-quantity > div.product-stock > strong").text());
							jsonGenerator.writeStringField("description", doc.select("#product_desc_"+id8digit).text());						    

							jsonGenerator.writeObjectFieldStart("guarantee");
							jsonGenerator.writeStartArray();
							Elements ul = doc.select("ul.safe-points");
							Elements li = ul.select("li");
							for (int i = 0; i < li.size(); i++) {
								jsonGenerator.writeString(li.get(i).text());
							}
							jsonGenerator.writeEndArray();
							jsonGenerator.writeEndObject();

							jsonGenerator.writeObjectFieldStart("product_spec");
						    Elements specs = doc.select("#product_spec_"+id8digit);
						    for (Element spec : specs) {
							    Elements dl = spec.select("dl.product-spec.kvp");
								for (int i = 0; i < dl.size(); i++) {
									Elements dt = dl.select("dt.kvp__key");
									Elements dd = dl.select("dd.kvp__value");
									for (int j = 0; j < dt.size(); j++) {
										jsonGenerator.writeStringField(dt.get(j).text(), dd.get(j).text());
									}
								}			    	
						    }				
													    
							Element pelapakInput = doc.select("li.vert-nav-item").first();
							String pelapak = pelapakInput.attr("data-insert-before-url");
							pelapak = pelapak.replace("/users/", "/");
							index = pelapak.indexOf("/");
							pelapak = pelapak.substring(index+1, pelapak.lastIndexOf("/"));
							jsonGenerator.writeStringField("seller_notes", doc.select("##user_tc_"+pelapak).text());						    
						    
							jsonGenerator.writeEndObject(); // closing root
															// object

							jsonGenerator.flush();
							jsonGenerator.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}										
				}
			}
		}
	}

	public static String getDomainName(URL url) {
		String strDomain = null;
		if (url != null) {
			String[] strhost = url.getHost().split(Pattern.quote("."));
			String[] strTLD = { "sg", "co.id", "com", "org", "net", "int", "edu", "gov", "mil", "arpa" };

			if (Arrays.asList(strTLD).indexOf(strhost[strhost.length - 1]) >= 0)
				strDomain = strhost[strhost.length - 2] + "." + strhost[strhost.length - 1];
			else if (strhost.length > 2)
				strDomain = strhost[strhost.length - 3] + "." + strhost[strhost.length - 2] + "."
						+ strhost[strhost.length - 1];
			else
				strDomain = strhost[strhost.length - 2] + "." + strhost[strhost.length - 1];
			return strDomain;
		}
		return strDomain;
	}

	/**
	 * Convert a string based locale into a Locale Object. Assumes the string
	 * has form "{language}_{country}_{variant}". Examples: "en", "de_DE",
	 * "_GB", "en_US_WIN", "de__POSIX", "fr_MAC"
	 * 
	 * @param localeString
	 *            The String
	 * @return the Locale
	 */
	public static Locale getLocaleFromString(String localeString) {
		if (localeString == null) {
			return null;
		}
		localeString = localeString.trim();
		if (localeString.toLowerCase().equals("default")) {
			return Locale.getDefault();
		}

		// Extract language
		int languageIndex = localeString.indexOf('_');
		String language = null;
		if (languageIndex == -1) {
			// No further "_" so is "{language}" only
			return new Locale(localeString, "");
		} else {
			language = localeString.substring(0, languageIndex);
		}

		// Extract country
		int countryIndex = localeString.indexOf('_', languageIndex + 1);
		String country = null;
		if (countryIndex == -1) {
			// No further "_" so is "{language}_{country}"
			country = localeString.substring(languageIndex + 1);
			return new Locale(language, country);
		} else {
			// Assume all remaining is the variant so is
			// "{language}_{country}_{variant}"
			country = localeString.substring(languageIndex + 1, countryIndex);
			String variant = localeString.substring(countryIndex + 1);
			return new Locale(language, country, variant);
		}
	}

	public static String fetchUrl(String strUrl) {
		String output = "";
		String line = null;
		try {

			URL url = new URL(strUrl);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			while ((line = reader.readLine()) != null) {
				output += line;
			}
			reader.close();

		} catch (MalformedURLException e) {
			System.out.println("ERROR CATCHED: " + e.getMessage());
			return null;
		} catch (IOException e) {
			System.out.println("ERROR CATCHED: " + e.getMessage());
			return null;
		}

		return output;
	}

}
