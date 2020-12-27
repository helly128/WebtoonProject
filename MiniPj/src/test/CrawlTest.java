package test;

import java.util.ArrayList;

import pj.toon.dao_hr.WebtoonDao;
import pj.toon.vo.WebtoonVo;
import pj.toon.webcrawling.KakaoCrawling;
import pj.toon.webcrawling.NaverCrawling;
import pj.toon.webcrawling.RidiCrawling;

public class CrawlTest {
	public static ArrayList<WebtoonVo> crawlTest() {
		ArrayList<WebtoonVo> list = new ArrayList<WebtoonVo>();
		NaverCrawling.crawl(list);
		KakaoCrawling.crawl(list);
		RidiCrawling.crawl(list);
		

		WebtoonDao dao = new WebtoonDao();
		dao.insert(list);
		
		return list;
	}
	
	public static void main(String[] args) {
		crawlTest();
	}
}
