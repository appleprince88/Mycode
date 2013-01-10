package fudan.java.filepath;

import java.io.*;

/**
 * @author <a href="mailto:huaxiajin8889@126.com">appleprince</a><br>
 * created on 2013-1-10 下午4:42:44
 */
public class AttainFilePath {

	
	public static String attainRootPath()
	{
		String path=AttainFilePath.class.getResource("./").toString();
		//判断是一般的java程序还是Web程序
		int index=path.indexOf("/WEB-INF");
		if(index==-1)
		{
			index=path.indexOf("/bin");
		}
		path=path.substring(0,index);
		
		if(path.startsWith("jar"))
		{
		 // 当class文件在jar文件中时，返回"jar:file:/F:/ ..."样的路径 
			path=path.substring(10);
		}else if(path.startsWith("file"))
		{
			// 当class文件在class文件中时，返回"file:/F:/ ..."样的路径 
			path=path.substring(6);
		}
		
		if(path.endsWith("/"))
		{
			path=path.substring(0,path.length()-1);
		}
		return path;
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		System.out.println(AttainFilePath.attainRootPath());
		String path=AttainFilePath.attainRootPath()+"/src/fudan/java/filepath/AttainFilePath.java";
		FileReader fs=new FileReader(path);
		BufferedReader br=new BufferedReader(fs);
		String line=null;
		while((line=br.readLine())!=null)
		{
			System.out.println(line);
		}
		
	}

}
