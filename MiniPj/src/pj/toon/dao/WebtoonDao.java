package pj.toon.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import pj.toon.vo.KeywordVo;
import pj.toon.vo.ReviewVo;
import pj.toon.vo.WebtoonVo;

public class WebtoonDao extends DAO {
	private PreparedStatement psmt;
	private ResultSet rs;
	private ResultSet rs2;

	// 검색어, 제목, 작가명, 썸네일, 플랫폼, 장르
//	private final String SEARCH = "SELECT toon_name, toon_writer, toon_pic, toon_site, toon_genre FROM webtoon WHERE toon_name like ? OR toon_writer like ?";
	private final String SEARCH = "SELECT toon_name, toon_writer, toon_pic, toon_site, toon_genre FROM webtoon WHERE toon_name like ?";
	private final String INSERT = "INSERT INTO webtoon VALUES(toon_seq.nextval, ?, ?, ?, ?, 0, 0, ?, 0, ?)";
	private final String COUNT = "SELECT COUNT(*) FROM webtoon WHERE toon_name like ? or toon_writer like ?";
	private final String GENRE_CNT = "SELECT COUNT(*) FROM webtoon WHERE toon_genre = ?";
	private final String SELECT_ALL = "SELECT NVL(R.STAR, 0) avgstar, NVL(R.CNT, 0) creview, W.*\n"
			+ "FROM WEBTOON W LEFT OUTER JOIN (SELECT COUNT(*) CNT, ROUND(AVG(REVIEW_STAR), 2) STAR, TOON_NO FROM REVIEW GROUP BY TOON_NO) R\n"
			+ "ON w.toon_no=r.toon_no ORDER BY TOON_VIEW DESC";
//	private final String SELECT_ALL = "SELECT * FROM WEBTOON ORDER BY TOON_VIEW DESC";
	private final String SELECT_ONE = "SELECT * FROM WEBTOON WHERE TOON_NO = ? ";
	private final String VIEWUP = "UPDATE WEBTOON SET TOON_VIEW = TOON_VIEW+1 WHERE TOON_NO=?";
	private final String REVIEW_CNT = "SELECT COUNT(*) FROM review WHERE toon_no = ? GROUP BY toon_no";
	private final String KEYWORD = "SELECT k_name FROM keyword WHERE review_no = ?";
	private final String DELETE_REV = "DELETE FROM review WHERE review_no = ?";
	private final String DELETE_KEY = "DELETE FROM keyword WHERE review_no = ?";
	private final String SEARCH_REV = "SELECT toon_no FROM review WHERE review_no = ?";
	private final String INSERT_KEY = "INSERT INTO keyword VALUES(k_seq.nextval, ?, ?, r_seq.currval)";
	private final String ORDER_KEY = "SELECT * FROM (SELECT k_name, count(*) FROM keyword WHERE toon_no = ? " + 
			"GROUP BY k_name ORDER BY 2 DESC) where rownum <= 3";
	
	
	
	public ArrayList<String> getKeywordOrder(int toon_no) {
		ArrayList<String> keyList = new ArrayList<String>();
		
		try {
			psmt = conn.prepareStatement(ORDER_KEY);
			psmt.setInt(1, toon_no);
			
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				keyList.add(rs.getString("k_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return keyList;
	}
	
	public int insertKeyword(int toon_no, String keyword) {
		int n = 0;
		try {
			psmt = conn.prepareStatement(INSERT_KEY);
			psmt.setString(1, keyword);
			psmt.setInt(2, toon_no);
			
			n = psmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n;
	}
	
	public int deleteReview(int review_no) {
		int n = 0;
		
		try {
			psmt = conn.prepareStatement(DELETE_REV);
			psmt.setInt(1, review_no);
			
			n = psmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n;
	}
	
	public int deleteKeyword(int review_no) {
		int n = 0;
		
		try {
			psmt = conn.prepareStatement(DELETE_KEY);
			psmt.setInt(1, review_no);
			
			n = psmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n;
	}
	
	public int selectReview(int review_no) {
		int toon_no = 0;
		try {
			psmt = conn.prepareStatement(SEARCH_REV);
			psmt.setInt(1, review_no);
			
			rs = psmt.executeQuery();
			if(rs.next()) {
				toon_no = rs.getInt("toon_no");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return toon_no;
	}
	
	public ArrayList<String> getKeyword(int review_no) {
		ArrayList<String> keyList = new ArrayList<String>();
		
		try {
			psmt = conn.prepareStatement(KEYWORD);
			psmt.setInt(1, review_no);
			
			rs2 = psmt.executeQuery();
			
			while(rs2.next()) {
				keyList.add(rs2.getString("k_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return keyList;
	}
	
	public ArrayList<ReviewVo> getReviewList(ReviewVo vo, int start) {
		ArrayList<ReviewVo> list = new ArrayList<ReviewVo>();
		
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT b.* FROM ");
			sql.append("(SELECT rownum rnum, a.* FROM (SELECT * FROM review ORDER BY review_no DESC) a ");
			sql.append("WHERE toon_no = ?) b ");
			sql.append("WHERE rnum>=? and rnum<=? ORDER BY review_no DESC");
			
			psmt = conn.prepareStatement(sql.toString());
			psmt.setInt(1, vo.getToon_no());
			psmt.setInt(2, start);
			psmt.setInt(3, start + 2);
			
			rs = psmt.executeQuery();
			while (rs.next()) {
				vo = new ReviewVo();
				vo.setToon_no(rs.getInt("toon_no"));
				vo.setReview_no(rs.getInt("review_no"));
				vo.setReview_content(rs.getString("review_content"));
				vo.setReview_star(rs.getInt("review_star"));
				vo.setNickname(rs.getString("nickname"));
				vo.setPassword(rs.getString("password"));
				
				ArrayList<String> keyList = getKeyword(rs.getInt("review_no"));
				vo.setKeyList(keyList);
				
				list.add(vo);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	public int getReviewCount(ReviewVo vo, int start) {
		int result = 0;
		
		try {
			psmt = conn.prepareStatement(REVIEW_CNT);
			psmt.setInt(1, vo.getToon_no());
			
			rs = psmt.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result; 
	}
	
	//보러가기버튼 count증가
	public WebtoonVo viewWeb(WebtoonVo vo) {
		try {
			
			psmt = conn.prepareStatement(SELECT_ONE);
			psmt.setInt(1, vo.getToon_no());
			rs = psmt.executeQuery();
			
			if(rs.next()) {
				psmt = conn.prepareStatement(VIEWUP);
				psmt.setInt(1, vo.getToon_no());
				psmt.execute();
				vo = new WebtoonVo();
				vo.setToon_no(rs.getInt("toon_no"));
				vo.setToon_link(rs.getString("toon_link"));
				vo.setToon_view(rs.getInt("toon_view"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return vo;
	}
	
	///수정 희원
	private final String SELECT_DETAIL = 
			"SELECT NVL(R.STAR, 0) star, NVL(R.CNT, 0) cnt, W.* " + 
			"FROM WEBTOON W LEFT OUTER JOIN (SELECT COUNT(*) CNT, ROUND(AVG(REVIEW_STAR), 2) STAR, TOON_NO FROM REVIEW GROUP BY TOON_NO) R " + 
			"ON w.toon_no=r.toon_no " + 
			"WHERE w.toon_no = ? " + 
			"ORDER BY TOON_VIEW DESC";
	
	public WebtoonVo select_detail(WebtoonVo vo) {
		try {
			psmt = conn.prepareStatement(SELECT_DETAIL);
			psmt.setInt(1, vo.getToon_no());
			rs = psmt.executeQuery();
			if(rs.next()) {
				vo = new WebtoonVo();
				vo.setToon_no(rs.getInt("toon_no"));
				vo.setToon_name(rs.getString("toon_name"));
				vo.setToon_writer(rs.getString("toon_writer"));
				vo.setToon_pic(rs.getString("toon_pic"));
				vo.setToon_site(rs.getString("toon_site"));
				vo.setToon_genre(rs.getString("toon_genre"));
				vo.setToon_link(rs.getString("toon_link"));
				vo.setToon_view(rs.getInt("toon_view"));
				vo.setAvg_star(rs.getDouble(1));
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return vo;
	}
		
		
		private final String INSERT_REVIEW = 
				"INSERT INTO review VALUES(r_seq.NEXTVAL, ?, ?, ?, ?, ?)";
		
		public int INSERT_REVIEW(ReviewVo vo) {
			int n = 0;
			try {
				psmt = conn.prepareStatement(INSERT_REVIEW);
				psmt.setString(1, vo.getReview_content());
				psmt.setInt(2, vo.getReview_star());
				psmt.setInt(3, vo.getToon_no());
				psmt.setString(4, vo.getNickname());
				psmt.setString(5, vo.getPassword());
				n = psmt.executeUpdate();
				
			}catch (SQLException e) {
				e.printStackTrace();
			}
			
			return n;
		}
		
	
		private final String INSERT_KEYWORD = 
				"INSERT INTO keyword VALUES(k_seq.NEXTVAL, ?, ?, r_seq.currval)"; 
		
		public int insert_keyword(KeywordVo vo) {
			int n = 0;
			try {
				psmt = conn.prepareStatement(INSERT_KEYWORD);
				psmt.setString(1, vo.getK_name());
				psmt.setInt(2, vo.getToon_no());
				n = psmt.executeUpdate();
				
			}catch (SQLException e) {
				e.printStackTrace();
			}finally {
				close();
			}
			
			return n;
		}

	public int getGenreListCount(HashMap<String, Object> listOpt){
		int result = 0;
		String genreKor = (String) listOpt.get("genreKor");

		try {
			psmt = conn.prepareStatement(GENRE_CNT);
			psmt.setString(1, genreKor);

			rs = psmt.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public ArrayList<WebtoonVo> getGenreList(HashMap<String, Object> listOpt){
		ArrayList<WebtoonVo> list = new ArrayList<WebtoonVo>();
		WebtoonVo vo;
		String genreKor = (String) listOpt.get("genreKor");
		int start = (Integer) listOpt.get("start");
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT b.* FROM ");
			sql.append("(SELECT rownum rnum, a.* FROM (SELECT * FROM webtoon ORDER BY toon_view DESC, toon_name) a ");
			sql.append("WHERE toon_genre = ?) b ");
			sql.append("WHERE rnum>=? and rnum<=? ORDER BY toon_view DESC, toon_name");

			psmt = conn.prepareStatement(sql.toString());
			psmt.setString(1, genreKor);
			psmt.setInt(2, start);
			psmt.setInt(3, start + 14);

			rs = psmt.executeQuery();
			while (rs.next()) {
				vo = new WebtoonVo();
				
				vo.setToon_no(rs.getInt("toon_no"));
				vo.setToon_name(rs.getString("toon_name"));
				vo.setToon_writer(rs.getString("toon_writer"));
				vo.setToon_pic(rs.getString("toon_pic"));
				vo.setToon_site(rs.getString("toon_site"));
				vo.setToon_genre(rs.getString("toon_genre"));
//				vo.setToon_link(rs.getString("toon_link"));
//				만약 별점평균 넣을거면 vo에 별점평균 변수 만들고, toon_no 가져와서 
				/*
				 * select r.star, w.*
from webtoon w,
  (select round(avg(review_star), 2) star, toon_no from review
   where toon_no = 1
   group by toon_no) r
where w.toon_no = r.toon_no;
이거 하
				 */
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}
	
	
	
	public ArrayList<WebtoonVo> selectAll() {
		ArrayList<WebtoonVo> list = new ArrayList<WebtoonVo>();
		
		try {
			WebtoonVo vo = new WebtoonVo();
			psmt = conn.prepareStatement(SELECT_ALL);
			rs = psmt.executeQuery();

			while (rs.next()) {
				vo = new WebtoonVo();
				vo.setToon_no(rs.getInt("toon_no"));
				vo.setToon_name(rs.getString("toon_name"));
				vo.setToon_writer(rs.getString("toon_writer"));
				vo.setToon_site(rs.getString("toon_site"));
				vo.setToon_pic(rs.getString("toon_pic"));
				vo.setToon_link(rs.getString("toon_link"));
				vo.setReview_star(rs.getInt("avgstar"));
				vo.setReview_star(rs.getInt("avgstar"));
				vo.setCount_review(rs.getInt("creview"));
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}

	public ArrayList<WebtoonVo> search(String search) {
		ArrayList<WebtoonVo> list = new ArrayList<WebtoonVo>();
		WebtoonVo vo;
		try {
			psmt = conn.prepareStatement(SEARCH);
			psmt.setString(1, "%" + search + "%");
//			psmt.setString(2, "%" + search + "%");
			rs = psmt.executeQuery();
			while (rs.next()) {
				vo = new WebtoonVo();
				vo.setToon_name(rs.getString("toon_name"));
				vo.setToon_writer(rs.getString("toon_writer"));
				vo.setToon_pic(rs.getString("toon_pic"));
				vo.setToon_site(rs.getString("toon_site"));
				vo.setToon_genre(rs.getString("toon_genre"));
//				vo.setToon_link(rs.getString("toon_link"));
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}

	public ArrayList<WebtoonVo> getSearchList(HashMap<String, Object> listOpt) {
		ArrayList<WebtoonVo> list = new ArrayList<WebtoonVo>();
		WebtoonVo vo;
		String searchBox = (String) listOpt.get("searchBox");
		int start = (Integer) listOpt.get("start");
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT toon_name, toon_writer, toon_pic, toon_site, toon_genre, toon_no FROM");
			sql.append("(SELECT rownum rnum, a.* FROM (SELECT * FROM webtoon ORDER BY toon_no) a ");
			sql.append("WHERE toon_name LIKE ? OR toon_writer LIKE ?) ");
			sql.append("WHERE rnum>=? and rnum<=? ORDER BY toon_no");

			psmt = conn.prepareStatement(sql.toString());
			psmt.setString(1, "%" + searchBox + "%");
			psmt.setString(2, "%" + searchBox + "%");
			psmt.setInt(3, start);
			psmt.setInt(4, start + 14);

			rs = psmt.executeQuery();
			while (rs.next()) {
				vo = new WebtoonVo();
				
				vo.setToon_no(rs.getInt("toon_no"));
				vo.setToon_name(rs.getString("toon_name"));
				vo.setToon_writer(rs.getString("toon_writer"));
				vo.setToon_pic(rs.getString("toon_pic"));
				vo.setToon_site(rs.getString("toon_site"));
				vo.setToon_genre(rs.getString("toon_genre"));
//				vo.setToon_link(rs.getString("toon_link"));
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}

	public int insert(ArrayList<WebtoonVo> list) {
		int n = 0;

		try {
			for (WebtoonVo vo : list) {
				psmt = conn.prepareStatement(INSERT);
				psmt.setString(1, vo.getToon_name());
				psmt.setString(2, vo.getToon_writer());
				psmt.setString(3, vo.getToon_site());
				psmt.setString(4, vo.getToon_genre());
				psmt.setString(5, vo.getToon_pic());
				psmt.setString(6, vo.getToon_link());

				n = psmt.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return n;
	}

	public int getListCount(HashMap<String, Object> listOpt) {
		// 글의 개수를 가져오는 메소드
		int result = 0;
		String searchBox = (String) listOpt.get("searchBox");

		try {
			psmt = conn.prepareStatement(COUNT);
			psmt.setString(1, "%" + searchBox + "%");
			psmt.setString(2, "%" + searchBox + "%");

			rs = psmt.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void close() {
		try {
			if (rs != null)
				rs.close();
			if (psmt != null)
				psmt.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
