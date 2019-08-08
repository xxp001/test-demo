package com.supervision.document.util;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Document;
import sun.nio.cs.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/*
 * @Project:SupervisionSystem
 * @Description:word to html
 * @Author:TjSanshao
 * @Create:2019-04-29 16:57
 *
 **/
public class Word2HtmlUtils {

    // f是需要转换的docx文件，filepath是存放图片的目录
    public static String Word2007ToHtml(File f, String filepath) throws IOException {
        if (!f.exists()) {
            return "Sorry File does not Exists!";
        } else {
            if (f.getName().endsWith(".docx") || f.getName().endsWith(".DOCX")) {

                // 1) 加载word文档生成 XWPFDocument对象
                InputStream in = new FileInputStream(f);
                XWPFDocument document = new XWPFDocument(in);

                // 2) 解析 XHTML配置 (这里设置IURIResolver来设置图片存放的目录)
                File imageFolderFile = new File(filepath);
                XHTMLOptions options = XHTMLOptions.create().URIResolver(new FileURIResolver(imageFolderFile));
                options.setExtractor(new FileImageExtractor(imageFolderFile));
                options.setIgnoreStylesIfUnused(false);
                options.setFragment(true);

                // 3) 将 XWPFDocument转换成XHTML
//                OutputStream out = new FileOutputStream(new File(filepath + htmlName));
//                XHTMLConverter.getInstance().convert(document, out, options);

                //也可以使用字符数组流获取解析的内容
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XHTMLConverter.getInstance().convert(document, baos, options);
                String content = baos.toString("UTF-8");
                baos.close();
                return content;
            } else {
                return "Enter only MS Office docx！";
            }
        }
    }

    // file是需要转换的doc文件，imagepath是存放图片的目录
    public static String Word2003ToHtml(File file, String imagepath) throws IOException, TransformerException, ParserConfigurationException {
        InputStream input = new FileInputStream(file);
        HWPFDocument wordDocument = new HWPFDocument(input);
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        //设置图片存放的位置
        wordToHtmlConverter.setPicturesManager(new PicturesManager() {
            public String savePicture(byte[] content, PictureType pictureType, String suggestedName, float widthInches, float heightInches) {
                File imgPath = new File(imagepath);
                if(!imgPath.exists()){//图片目录不存在则创建
                    imgPath.mkdirs();
                }
                File file = new File(imagepath + suggestedName);
                try {
                    OutputStream os = new FileOutputStream(file);
                    os.write(content);
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return imagepath + suggestedName;
            }
        });

        // 转换
        wordToHtmlConverter.processDocument(wordDocument);
        Document htmlDocument = wordToHtmlConverter.getDocument();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DOMSource domSource = new DOMSource(htmlDocument);
        StreamResult streamResult = new StreamResult(outStream);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");//编码格式
        serializer.setOutputProperty(OutputKeys.INDENT, "no");//是否用空白分割
        serializer.setOutputProperty(OutputKeys.METHOD, "html");//输出类型
        serializer.transform(domSource, streamResult);
        String content = new String(outStream.toByteArray(), "UTF-8");
        outStream.close();
        return content;
    }

    public static boolean HtmlToWord(String html, String filePath) throws IOException {

        File file = new File(filePath);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (file.getName().endsWith(".docx")) {
            return false;
        }

        byte[] bytes = html.getBytes("utf-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        POIFSFileSystem poifsFileSystem = new POIFSFileSystem();
        DirectoryEntry root = poifsFileSystem.getRoot();
        DocumentEntry document = root.createDocument("WordDocument", byteArrayInputStream);
        FileOutputStream outputStream = new FileOutputStream(file);
        poifsFileSystem.writeFilesystem(outputStream);
        byteArrayInputStream.close();
        outputStream.close();
        return true;
    }

    public static void main(String[] args) throws IOException, TransformerException, ParserConfigurationException {
        System.out.println(Word2HtmlUtils.Word2003ToHtml(new File("C:\\supervision\\测试新文档\\test\\test-001.doc"), ""));;
    }

}
