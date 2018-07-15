package cn.itcast.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * lucene的查询
 * 		总体分为两种查询方式：
 * 			Query的子类查询（TermQuery，NumericRangeQuery，3.2.3. BooleanQuery）
 * 			QueryParse解析表达式查询
 * @author 张光宗
 *
 */
public class TestLuceneSearch {
	
	private IndexSearcher indexSearcher;
	/**
	 * @description 初始化IndexSearcher
	 * @return
	 */
	@Before
	public void init() throws IOException{
		Directory directory = FSDirectory.open(new File("d:/upload/indexDB"));
		//创建indexReader
		IndexReader indexReader = DirectoryReader.open(directory);
		indexSearcher = new IndexSearcher(indexReader);
	}
	
	/**
	 * @description 由于显示查询结果的过程都是一样的，这里抽取一个方法
	 * @return
	 */
	public void searchResult(Query query) throws IOException{
		TopDocs topDocs = indexSearcher.search(query, 10);
		System.out.println("总条数："+topDocs.totalHits);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			//获取文档id
			int docID = scoreDoc.doc;
			Document doc = indexSearcher.doc(docID);
			System.out.println(doc.get("name"));
			System.out.println(doc.get("path"));
			System.out.println(doc.get("size"));
//			System.out.println(doc.get("content"));
		}
	}
	
	/**
	 * @description 查询索引库  基本的查寻  TermQuery，
	 * 				通过指定域指定关键字，后续会有具体的两种查询方式
	 * @return
	 */
	@Test
	public void testTermQuery() throws IOException{
		//创建查询对象
		Query query = new TermQuery(new Term("name","apache"));
		searchResult(query);
		indexSearcher.getIndexReader().close();
	}
	
	/**
	 * @description 查询索引库 ，查询所有  MatchAllDocsQuery
	 * @return
	 */
	@Test
	public void testMatchAllDocsQuery() throws IOException{
		//创建查询对象
		Query query = new MatchAllDocsQuery();
		searchResult(query);
		indexSearcher.getIndexReader().close();
	}

	/**
	 * @description 查询索引库 ，根据文件的长度查询  NumericRangeQuery
	 * 				我们的size类型是Long所以这里使用newLongRange
	 * 				如果是其他类型可以根据实际情况
	 * @return
	 */
	@Test
	public void testNumericRangeQuery() throws IOException{
		//创建查询对象
		//123参数表示  size的大小从 0 到 1000 
		//45参数表示  包括0 和 1000
		Query query = NumericRangeQuery.newLongRange("size", 0L, 1000L, true, true);
		searchResult(query);
		indexSearcher.getIndexReader().close();
	}
	
	/**
	 * @description 查询索引库 ，多条件组合查询BooleanQuery
	 * @return
	 */
	@Test
	public void testBooleanQuery() throws IOException{
		//创建查询对象
		//123参数表示  size的大小从 0 到 1000 
		//45参数表示  包括0 和 1000
		BooleanQuery query = new BooleanQuery();
		//查询条件：
		TermQuery termQuery1 = new TermQuery(new Term("name","apache"));
		TermQuery termQuery2 = new TermQuery(new Term("content","apache"));
		//将查询条件添加进BooleanQuery
		//Occur.MUST表示必须满足条件   相当于and
		//Occur.SHOULD表示应该满足条件   相当于or
		//Occur.MUST_NOT表示必须满足条件   相当于and
		query.add(termQuery1, Occur.SHOULD);
		query.add(termQuery2, Occur.MUST);
		searchResult(query);
		indexSearcher.getIndexReader().close();
	}
	
	
	/**
	 * @description 第二种查询方式：QueryParser
	 * 				带分析的查询，比如百度搜索，
	 * 				我们在搜索框中输入一句话，他可以自动帮我们拆分成N个关键词来查询
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 */
	@Test
	public void testQueryParser() throws Exception{
		//创建QueryParser对象
		//参数一：指定默认域
		//参数二：使用什么分词器     原理会把整个句子解析
		QueryParser queryParser = new QueryParser("content", new IKAnalyzer());
//		Query query = queryParser.parse("lucene是一个基于Java开发的");
		//查询语法name:apache
		//如果不指定的话，会使用上面的“content”的默认域
		Query query = queryParser.parse("name:apache");
		//查询
		searchResult(query);
	}
	
	/**
	 * @description 
	 * @return
	 */
	@Test
	public void testMulitFieldQueryParser() throws Exception {
		//创建MultiFieldQueryParser对象
		//参数一：指定默认域   数组（也就是多个默认域）
		//参数二：使用什么分词器     原理会把整个句子解析
		String[] fields = {"name","content","size"};
		//创建一个MultiFieldQueryParser对象
		MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, new IKAnalyzer());
		//使用对象创建一个Query对象
		//如果不指定的话，会使用上面的fields数组中的默认域（这里没有指定）
		Query query = queryParser.parse("lucene是一个基于java开发的全文检索工具包");
		System.out.println(query);
		//执行查询
		searchResult(query);
		
	}
	/**
	 * lucene的查询语法
		1、基础查询语法
			域名:关键词
			例如：name:apache
		2、查询全部文档
			*:*
		3、范围查询语法
			域名:[最小值 TO 最大值]
			例如：size:[0 TO 2000]
			[]:包含边界值
			{}:不包含边界值
			在QueryParser中数值范围查询是不可以使用的，查询数值范围应该使用NumericRangeQuery。
			仅限字符串范围。在solr中完全支持。
		4、多条件组合查询
			两个条件必须满足，使用“+”作为前缀
				+name:apache +content:apache		或者 name:apache AND content:apache
			两个条件满足其一即可，条件前无前缀
				name:apache content:apache			或者 name:apache OR content:apache
			不能满足条件，条件前使用“-”作为前缀
				-name:apache content:apache			或者 NOT name:apache content:apache
	 */
}



















