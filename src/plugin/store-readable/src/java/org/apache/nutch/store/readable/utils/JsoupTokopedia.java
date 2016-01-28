package org.apache.nutch.store.readable.utils;

import org.apache.nutch.fetcher.DomainConstants;
import org.apache.nutch.fetcher.JsoupUtil;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.Bytes;
import org.apache.wicket.ajax.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by harji on 1/20/16.
 */
public class JsoupTokopedia {

    public static final Logger LOG = LoggerFactory.getLogger(JsoupTokopedia.class);

    public static String getDomainName(URL url) {
        String strDomain = "tokopedia.com.";
//        if (url != null) {
//            String[] strhost = url.getHost().split(Pattern.quote("."));
//            String[] strTLD = {"sg", "co.id", "com", "org", "net", "int", "edu", "gov", "mil", "arpa"};
//
//            if (Arrays.asList(strTLD).indexOf(strhost[strhost.length - 1]) >= 0)
//                strDomain = strhost[strhost.length - 2] + "." + strhost[strhost.length - 1];
//            else if (strhost.length > 2)
//                strDomain = strhost[strhost.length - 3] + "." + strhost[strhost.length - 2] + "."
//                        + strhost[strhost.length - 1];
//            else
//                strDomain = strhost[strhost.length - 2] + "." + strhost[strhost.length - 1];
//            return strDomain;
//        }
        return strDomain;
    }

    public JSONObject constructJson(Content content) throws JSONException {
        // jsoup
        JSONObject jsonGenerator = new JSONObject();
        if (content != null) {
//            HttpWebClient webClient = new HttpWebClient();
//            String pageSource = "";//webClient.getHtmlPage(content.getBaseUrl(), null, null, null);
//            Document doc = Jsoup.parse(pageSource);
			Document doc = Jsoup.parse(Bytes.toStringBinary(content.getContent()));
            if (doc != null) {
                URL url = null;
                try {
                    url = new URL(content.getBaseUrl());
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    LOG.warn("ERROR url ", e1);
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
                    String category = doc.select("h2 a").text();//.size() > 0 ? odc.select("h2 a").get(0).text() : null;
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
                            String pageSourceTalk = "";//webClient.getHtmlPage(content.getBaseUrl(), null, null, "/talk");
                            Document docTalk = Jsoup.parse(Bytes.toStringBinary(content.getContent()));//Jsoup.parse(pageSourceTalk);
//							System.out.println(docTalk);

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
                                String path = homeDir + separator + "crawlers" + separator + "data" + separator + countryCode + separator + domainName + separator;
                                File dir = new File(path);
                                if (!dir.exists())
                                    dir.mkdirs();
                                String fileName = category.replaceAll("[^a-zA-Z0-9.-]", "") + "_" + domainName + "_"
                                        + countryCode + "_" + id8digit + "_" + out;

                                file = new FileWriter(path + fileName);



//                                JsonGenerator jsonGenerator = new JsonFactory().createGenerator(file);
                                // for pretty printing
//                                jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
//
//
//                                jsonGenerator.writeStartObject(); // start root
                                // object
                                jsonGenerator.put("country", countryCode);
                                jsonGenerator.put("site", url.getHost());
                                jsonGenerator.put("doc_date", sdf.format(date));
                                jsonGenerator.put("product_url", content.getBaseUrl().toString());
                                jsonGenerator.put("product_id", id8digit);
                                jsonGenerator.put("product_name", item);
                                jsonGenerator.put("category", category);
                                jsonGenerator.put("sub_category", subcategory);
                                jsonGenerator.put("sub_sub_category", subsubcategory);
                                jsonGenerator.put("price", doc.select("div[class=product-box text-center mb-10] div[itemprop=offers] span[itemprop=price]").text());
                                jsonGenerator.put("last_updated_price", doc.select("#product-20222448 > div.row-fluid > div.span3 > div.product-box.text-center.mb-10 > small.product-pricelastupdated > i").text());
                                jsonGenerator.put("transaction_success_rate", doc.select("#product-20222448 > div.row-fluid > div.span9 > div.span9.mt-15 > div > span > span:nth-child(1)").text());
                                jsonGenerator.put("transaction_total", doc.select("#product-20222448 > div.row-fluid > div.span9 > div.span9.mt-15 > div > span > span:nth-child(2)").text());

                                Elements productImages = doc.select("#product-20222448 > div.row-fluid > div.span9 > div.row-fluid > div.span5.product-image-holder > div.mt-5 > div.jcarousel.product-imagethumb-alt > ul");
                                int i = 1;
                                String imgName = "";
                                String imgPath = homeDir + separator + "crawlers" + separator + "data" + separator + countryCode + separator + domainName + separator + category + separator + "imgs" + separator + id8digit + separator;
                                File dirPath = new File(imgPath);

//                                JSONArray imgArray = new JSONArray();
//
//                                for (Element productImage : productImages){
//                                    Elements imgs = productImage.getElementsByTag("img");
//                                    for (Element img : imgs) {
//                                        imgName = id8digit + "." + i;
//                                        String src = img.attr("src");
////                                        JsoupUtil.getImages(src, imgPath, imgName);
//                                        jsonGenerator.writeString(imgName);
//                                        i++;
//                                    }
//                                }

//                                jsonGenerator.writeArrayFieldStart("product_imgs");
//                                for (Element productImage : productImages) {
//                                    Elements imgs = productImage.getElementsByTag("img");
//                                    for (Element img : imgs) {
//                                        imgName = id8digit + "." + i;
//                                        String src = img.attr("src");
////                                        JsoupUtil.getImages(src, imgPath, imgName);
//                                        jsonGenerator.writeString(imgName);
//                                        i++;
//                                    }
//                                }
//                                jsonGenerator.writeEndArray();


//                                jsonGenerator.writeObjectFieldStart("store");

                                Element shop = doc.select("input[name=shop_id]").first();
                                jsonGenerator.put("store_id", shop.attr("value"));
                                jsonGenerator.put("store_name", doc.select("a[itemprop=name]").text());
                                jsonGenerator.put("store_location", doc.select("span[itemprop=location]").text());
                                Elements reputations = doc.select("td[class=mt-5]");
                                i = 1;
//                                for (Element reputation : reputations) {
//                                    jsonGenerator.writeStartArray();
//                                    Elements imgs = reputation.getElementsByTag("img");
//                                    for (Element img : imgs) {
//                                        imgName = id8digit + "." + i;
//                                        String src = img.attr("src");
//                                        JsoupUtil.getImages(src, imgPath, imgName);
//                                        jsonGenerator.put("store_reputation", imgName);
//                                        i++;
//                                    }
//                                }
                                jsonGenerator.put("store_points", doc.select("div[class=clear-b mb-0 fs-12 lh-16 mt-10 reputasi-gm-product]").first().ownText());

                                jsonGenerator.put("brand", brand);
                                jsonGenerator.put("description",
                                        doc.select("p[itemprop=description]").text());
                                jsonGenerator.put("is_delivery",
                                        doc.select("div div div div div div li div small").size() > 0
                                                ? doc.select("div div div div div div li div small").get(0).text()
                                                : "");

                                jsonGenerator.put("instalpayment", doc.select("#trigger-instalpayment > small").text());

//                                jsonGenerator.writeObjectFieldStart("product_information");
                                JSONObject prdInformation= new JSONObject();
                                prdInformation.put("seen",
                                        doc.select("div div div dl dd.pull-left.m-0").size() > 0
                                                ? doc.select("div div div dl dd.pull-left.m-0").get(0).text() : "");
                                prdInformation.put("weight",
                                        doc.select("div div div dl dd.pull-left.m-0").size() > 1
                                                ? doc.select("div div div dl dd.pull-left.m-0").get(1).text() : "");
                                prdInformation.put("sold",
                                        doc.select("div div div dl dd.pull-left.m-0").size() > 2
                                                ? doc.select("div div div dl dd.pull-left.m-0").get(2).text() : "");
                                prdInformation.put("insurance",
                                        doc.select("div div div dl dd.pull-left.m-0").size() > 3
                                                ? doc.select("div div div dl dd.pull-left.m-0").get(3).text() : "");
                                prdInformation.put("condition",
                                        doc.select("div div div dl dd.pull-left.m-0").size() > 4
                                                ? doc.select("div div div dl dd.pull-left.m-0").get(4).text() : "");
                                prdInformation.put("min_order",
                                        doc.select("div div div dl dd.pull-left.m-0").size() > 5
                                                ? doc.select("div div div dl dd.pull-left.m-0").get(5).text() : "");

                                jsonGenerator.put("Product_information",prdInformation);

                                Elements links = doc.select("#product-24312555 > div.row-fluid > div.span9 > div.row-fluid > div.span5.product-image-holder > div.product-box > div.product-box-content > ul > li > a");
                                for (Element link : links) {
                                    String catalogId = link.attr("href");
                                    int indexId1 = catalogId.indexOf("/catalog/");
                                    catalogId = item.substring(indexId1 + 1);
                                    int indexId2 = catalogId.lastIndexOf("/");
                                    catalogId = item.substring(0, indexId2);
                                    jsonGenerator.put("catalog_id", catalogId);
                                    jsonGenerator.put("catalog_name", link.text());
                                }

                                JSONObject reviews = new JSONObject();

                                reviews.put("quality_avg",
                                        doc.select("div#review-summary div div div div div table tbody tr").size() > 0
                                                ? doc.select("div#review-summary div div div div div table tbody tr")
                                                .get(0).text()
                                                : "");

                                jsonGenerator.put("Reviews",reviews);

                                JSONObject commentThreads= new JSONObject();

//								String pageSourceTalk = webClient.getHtmlPage(content.getBaseUrl(), null, null, "/talk");
//								Document docTalk = Jsoup.parse(Bytes.toStringBinary(content.getContent()));//Jsoup.parse(pageSourceTalk);
//								System.out.println(docTalk);


                                Elements talks = docTalk.select("div[class=tab-content product-content-container] ul[class=list-box] li");
                                for (Element talk : talks) {
//                                    jsonGenerator.writeStartObject();
                                    Element talkId = talk.select("input[name=talk]").first();
                                    jsonGenerator.put("comments_thread_id", talkId.attr("value"));

                                      JSONArray comments = new JSONArray();
//                                    jsonGenerator.writeArrayFieldStart("comments");
                                    links = talk.select("div[class=list-box-replybuyer]");
                                    for (Element link : links) {
                                        JSONObject jsonObjectUid = new JSONObject();
//                                        jsonGenerator.writeStartObject();
                                        Elements as = link.select("a[class=pull-left]");

                                        for (Element a : as) {
                                            String catalogId = a.attr("href");
                                            int indexId1 = catalogId.indexOf("/people/");
                                            catalogId = catalogId.substring(indexId1 + 1);
                                            int indexId2 = catalogId.lastIndexOf("/");
                                            catalogId = catalogId.substring(indexId2 + 1);
                                            jsonObjectUid.put("user_id", catalogId);
                                            jsonObjectUid.put("user_name", link.select("small > b").first().text());
                                        }

                                        jsonObjectUid.put("user_type", link.select("small:nth-child(2) > span").text());

                                        Elements smileys = link.select("small:nth-child(2) > div > div.arrow_box_left.smile-tooltip-hover > table > tbody > tr");
                                        for (Element smiley : smileys) {
                                            jsonObjectUid.put(smiley.select("td").first().text().toLowerCase().replaceAll("f", "ve"), smiley.select("td").last().text());
                                        }
                                        Elements commentDates = link.select("small[class=muted] i");
                                        for (Element commentDate : commentDates) {
                                            jsonObjectUid.put("comment_date", commentDate.attr("title"));
                                        }

                                        jsonObjectUid.put("comment_text", link.select("div[class=list-box-sellerreplycontent]").text());
//                                        jsonGenerator.writeEndObject();

                                        Elements links2 = link.select("div[class=list-box-replyholder]");
                                        for (Element link2 : links2) {
//                                            jsonGenerator.writeStartObject();
                                            as = link2.select("a[class=pull-left]");
                                            for (Element a : as) {
                                                String catalogId = a.attr("href");
                                                int indexId1 = catalogId.indexOf("/people/");
                                                catalogId = catalogId.substring(indexId1 + 1);
                                                int indexId2 = catalogId.lastIndexOf("/");
                                                catalogId = catalogId.substring(indexId2 + 1);
                                                jsonObjectUid.put("user_reply_id", catalogId);
                                                jsonObjectUid.put("user_reply_name", link2.select("small > b").first().text());
                                            }
                                            jsonObjectUid.put("user_type", link2.select("span[class^=label]").text());

                                            if (smileys.size() == 0) {
                                                Element point = link2.select("img[class=pull-left icon-tooltip ml-5 mt-3]").first();
                                                if (point != null) {
                                                    String pointStr = point.attr("data-original-title");
                                                    jsonObjectUid.put("reply_points", pointStr);
                                                } else {
                                                    smileys = link2.select("div[class=arrow_box_left smile-tooltip-hover]");
                                                    for (Element smiley : smileys) {
                                                        jsonObjectUid.put(smiley.select("td").first().text().toLowerCase().replaceAll("f", "ve"), smiley.select("td").last().text());
                                                    }
                                                }
                                            }

                                            Elements commentDates2 = link2.select("small span[class=muted] i");
                                            for (Element commentDate2 : commentDates2) {
                                                jsonObjectUid.put("reply_comment_date", commentDate2.attr("title"));
                                            }
                                            jsonObjectUid.put("reply_comment_text", link2.select("div[class=list-box-sellerreplycontent]").text());
//                                            jsonGenerator.writeEndObject();
                                        }
                                       comments.put(jsonObjectUid.toString());
                                    }
                                    jsonGenerator.put("coments",comments);
//                                    jsonGenerator.writeEndArray();

//                                    jsonGenerator.writeEndObject();
                                }
//                                jsonGenerator.writeEndArray();

                                jsonGenerator.put("username", doc.select("a[itemprop=author").text());
                                jsonGenerator.put("comment",
                                        doc.select("span[itemprop=reviewBody]").text());
                                jsonGenerator.put("quality",
                                        doc.select("i[class=rating-star5 mt-3]").size() > 0
                                                ? doc.select("i[class=rating-star5 mt-3]").get(0).attr("title") : "");
                                jsonGenerator.put("accuracy",
                                        doc.select("i[class=rating-star5 mt-3]").size() > 1
                                                ? doc.select("i[class=rating-star5 mt-3]").get(1).attr("title") : "");
                                jsonGenerator.put("date",
                                        doc.select(
                                                "#review-container > li > div > div.list-box-comment > div.list-box-top > div > small > i")
                                                .text());


                                String json = String.valueOf(jsonGenerator);
                                System.out.println("json:"+json);

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                LOG.warn("ERROR FNFE " + domainName + " ", e);
                            } catch (IOException e) {
                                LOG.warn("ERROR IOE " + domainName + " ", e);
                                e.printStackTrace();
                            }
                        }
                }

            }
        }

        return jsonGenerator;
    }

}