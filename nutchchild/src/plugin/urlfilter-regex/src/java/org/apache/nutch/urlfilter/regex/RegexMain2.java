package org.apache.nutch.urlfilter.regex;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RegexMain2 {

	public static void main(String[] args) throws IllegalArgumentException, FileNotFoundException, IOException {
		// TODO Auto-generated method stub

        String filename = "/home/harji/Workspace/Project/BigData/Project-Src/crawlers/nutch-2.x/runtime/local/conf/regex-urlfilter.txt";
        String url_exclude = "https://www.tokopedia.com/p/kamera-foto-video/kamera{()";
        String url_include = "https://www.tokopedia.com/p/kamera-foto-video/kamera";

        RegexURLFilter regexFilter = new RegexURLFilter(new FileReader(filename));
        System.out.println("FILTER:"+regexFilter.filter(url_include));


	}

}
