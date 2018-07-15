package cn.itcast.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * 
 * lucene的简单使用
 * 	需要的jar包：
		lucene-core-4.10.3.jar
		lucene-analyzers-common-4.10.3.jar
		commons-io.jar
		IKAnalyzer2012FF_u1.jar
		lucene-queryparser-4.10.3.jar   带分析的查询
 * 	包括：
 * 		创建索引：testCreateIndex()
 * 		查询索引：testQueryIndex()
 * 		分词器：testCreateAnalyzer()  ---这里用的是第三方的工具  IKAnalyzer
 * 		其中分词器需要配置：在IKAnalyzer.cfg.xml中指定扩展词和停用词
 * 			IKAnalyzer.cfg.xml
			mydict.dic
			stopword.dic
			-------------------------------IKAnalyzer.cfg.xml配置如下
			<?xml version="1.0" encoding="UTF-8"?>
			<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">  
			<properties>  
				<comment>IK Analyzer 扩展配置</comment>
				<!--用户可以在这里配置自己的扩展字典 -->
				<entry key="ext_dict">mydict.dic;</entry> 
				<!--用户可以在这里配置自己的扩展停止词字典-->
				<entry key="ext_stopwords">stopword.dic;</entry> 
			</properties>
 * @author 张光宗
 *		
 */
public class TestLucene {
	
	/**
	 * @description 创建索引库
	 * @return
	 */
	@Test
	public void testCreateIndex() throws IOException{
		//创建Directory  索引库的位置
		Directory directory = FSDirectory.open(new File("d:/upload/indexDB"));
		//创建 IndexWriter
		//第三方分词器IKAnalyzer
		Analyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);
		//读取文件
		File dir = new File("D:/upload/searchsource");
		File[] listFiles = dir.listFiles();
		for (File file : listFiles) {
			//获取各中属性
			String fileName = file.getName();
			String filePath = file.getPath();
			String fileContent = FileUtils.readFileToString(file);
			long fileSize = FileUtils.sizeOf(file);
			//将属性添加到各种域中TextField:对数据进行分析，索引以及保存
			Field fieldName = new TextField("name", fileName, Store.YES);
			//StringField：对数据  不分析   但"索引"和"保存"
			Field fieldPath = new StringField("path", filePath, Store.YES);
			Field fieldContent = new TextField("content", fileContent, Store.NO);
			//LongField  保存一个Long型数据
			Field fieldSize = new LongField("size", fileSize, Store.YES);
			//将域添加到document文档中
			Document document = new Document();
			document.add(fieldName);
			document.add(fieldPath);
			document.add(fieldContent);
			document.add(fieldSize);
			
			indexWriter.addDocument(document);
		}
		//关闭indexWriter
		indexWriter.close();
	}
	
	
	/**
	 * @description 查询索引库  基本的查询，后续会有具体的两种查询方式
	 * @return
	 */
	@Test
	public void testQueryIndex() throws IOException{
		Directory directory = FSDirectory.open(new File("d:/upload/indexDB"));
		//创建indexReader
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		//创建查询对象
		Query query = new TermQuery(new Term("name","apache"));
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
	 * @description 创建分析器
	 * @return
	 */
	@Test
	public void testCreateAnalyzer() throws IOException{
		//创建一个分析器  IKAnalyzer第三方可以解析中文
		Analyzer analyzer = new IKAnalyzer();
		//参数一：可以为空  参数二：要分析的数据
		TokenStream tokenStream = analyzer.tokenStream(null, "Lucene经典教程");
		tokenStream.reset();
		//创建一个引用对象  相当于关键词的指针
		CharTermAttribute addAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		while(tokenStream.incrementToken()){
			System.out.println(addAttribute.toString());
		}
		tokenStream.close();
	}
	
	
	/**
	 * @description 添加文档到索引库
	 * @return
	 */
	@Test
	public void addDocument() throws IOException{
		//创建Directory  索引库的位置
		Directory directory = FSDirectory.open(new File("d:/upload/indexDB"));
		//创建 IndexWriter
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());
		IndexWriter indexWriter = new IndexWriter(directory, config);
		
		Document document = new Document();
		Field fieldName = new TextField("name1", "新添加文档", Store.YES);
		
		document.add(fieldName);
		indexWriter.addDocument(document);
		indexWriter.close();
	}
	
	
	/**
	 * @description 删除  所有 慎用
	 */
	@Test
	public void delDocumentAll() throws IOException{
		//创建Directory  索引库的位置
		Directory directory = FSDirectory.open(new File("d:/upload/indexDB"));
		//创建 IndexWriter
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());
		IndexWriter indexWriter = new IndexWriter(directory, config);
		
		indexWriter.deleteAll();
		indexWriter.close();
	}
	
	
	/**
	 * @description 通过查询删除，将查询到的结果删除
	 * @return
	 */
	@Test
	public void delDocumenByQuery() throws IOException{
		//创建Directory  索引库的位置
		Directory directory = FSDirectory.open(new File("d:/upload/indexDB"));
		//创建 IndexWriter
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());
		IndexWriter indexWriter = new IndexWriter(directory, config);
		Query query = new TermQuery(new Term("name","apache"));
		indexWriter.deleteDocuments(query );
		
		indexWriter.close();
	}
	
	/**
	 * 更新文档，同添加一样
	 * 原理：先删除后添加
	 */
}






















