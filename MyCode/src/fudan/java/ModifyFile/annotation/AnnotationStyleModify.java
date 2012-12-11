package fudan.java.ModifyFile.annotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

public class AnnotationStyleModify extends ModifyFile{

	public AnnotationStyleModify(String filename) throws FileNotFoundException
	{	
		super(filename);
	}
	public AnnotationStyleModify(String sourcePathname,String distPathname)
	{
		super(sourcePathname,distPathname);
	}
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
//			AnnotationStyleModify am=new AnnotationStyleModify("1.txt","G:"+File.separator+"s"+File.separator+"5.txt");
			AnnotationStyleModify am=new AnnotationStyleModify("1.txt");
			am.modifyFile();
			am.setDistPathname("2.txt");
			am.modifyFile();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected String modifyOneLine(String line) {
		// TODO Auto-generated method stub
		String result=null;
		int index=line.indexOf("//");
		//�����ж��Ƿ����
		if(index==-1)
		{
			result=line;
		}//�����ھ�ִ������Ĳ���
		else
		{
			String pre=line.substring(0, index);
			String post=null;
			if((index+2)<line.length())
				post=line.substring(index+2);
			if(post==null)
			{
				result=pre;
			}
			else
			{
				post="/**"+post+"*/";
				result=pre+post;
			}
		}
		return result;
	}


}
