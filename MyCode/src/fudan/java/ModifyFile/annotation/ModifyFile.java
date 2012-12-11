package fudan.java.ModifyFile.annotation;
import java.io.*;
import java.util.Vector;
public abstract class ModifyFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
	protected File getFile() throws IllegalArgumentException
	{
		File file=new File(sourcePathname);
		if(!file.exists())
		{
			throw new IllegalArgumentException("file is not exists");
		}
		return file;
	}
	/**
	 * 	��ȡ�ļ��е�һ��
	 * @return a line string of the file
	 * @throws IOException
	 */
	protected String readLine(BufferedReader br) throws IOException
	{
		String strline=br.readLine();
		
		return strline;
	}
	/**
	 * �޸��ļ�
	 * @throws IOException 
	 */
	public void modifyFile() throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(getFile()));
		Vector<String> tResult=new Vector<String>();
		String line=null;
		while((line=readLine(br))!=null)
		{
			line=this.modifyOneLine(line);
			tResult.add(line);
		}
		br.close();
		writeToFile(tResult);
	}
	
	/**
	 * ���޸ĵĶ���д���ļ���ȥ
	 * @param context ���޸ĺ���������飬ÿһ��item���ÿһ���޸ĵ���
	 * @throws IOException
	 */
	protected void writeToFile(Vector<String> context) throws IOException
	{
		File newfile=new File(distPathname);
		if(newfile.exists())
		{
			newfile.delete();
			System.out.println("�Ѵ��ڵ��ļ��ѱ�ɾ��");
		}
		newfile.createNewFile();
		BufferedWriter bw=new BufferedWriter(new FileWriter(newfile));
		int length=context.size();
		System.out.println("start write");
		int index=0;
		while(index<length)
		{
			bw.write(context.get(index));
			bw.newLine();
			index++;
		}
		bw.flush();
		bw.close();
		System.out.println("end write");
	}
	
	/**
	 * ��һ����Ҫ�޸ĵ������޸�Ϊ��Ҫ����ʽ
	 * @param line ���ļ��ж�ȡ����һ������
	 * @return
	 */
	protected abstract String modifyOneLine(String line);
	/**
	 * ���캯��
	 * ��������ṩ��Ĭ��Դ�ļ���Ŀ���ļ�ʱͬһ���ļ�
	 * @throws FileNotFoundException
	 * @param filename :the name of file
	 *  */
	public ModifyFile(String filename) throws FileNotFoundException
	{
		this.sourcePathname=filename;
		distPathname=filename;
	}
	/**
	 * ������캯��Դ�ļ���Ŀ���ļ�����ͬһ���ļ�
	 * @param sourcePathname
	 * @param distPathname
	 */
	public ModifyFile(String sourcePathname,String distPathname)
	{
		this.sourcePathname=sourcePathname;
		this.distPathname=distPathname;
		
	}
	public String getSourcePathname() {
		return sourcePathname;
	}
	public void setSourcePathname(String sourcePathname) {
		this.sourcePathname = sourcePathname;
	}
	public String getDistPathname() {
		return distPathname;
	}
	public void setDistPathname(String distPathname) {
		this.distPathname = distPathname;
	}
	private String sourcePathname;
	private String distPathname;
}
