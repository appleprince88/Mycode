/**
 * 
 */
package fudan.java.database.connectionPool;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @author appleprince
 *
 */
public class ConnectionPool {

	private static String jdbcdriver="";//驱动
	private static String url="";//连接字符串
	private static String username="";//连接的数据库的用户名
	private static String password="";//连接的数据库的密码
	private static int initialConnections=5;//连接池的初始数量
	private static int maxNumCOnnections=100;//连接池的最大数量
	private static int incrementalConnections=5;//每次增加的连接数量
	private static Queue<OneConnection> unUsedConnections=new LinkedList<OneConnection>();;//未用的连接
	private static List<OneConnection> usedConnections=new LinkedList<OneConnection>();//已用的连接
	private static ConnectionPool connectionPool=new ConnectionPool();
	public ConnectionPool(String driver,String url,String username,String password)
	{
		this.jdbcdriver=driver;
		this.url=url;
		this.username=username;
		this.password=password;
	}
	private ConnectionPool() 
	{
		InputStream in;
		try {
			in = new FileInputStream("properties/database.properties");
			Properties props=new Properties();
			props.load(in);
			this.jdbcdriver=props.getProperty("jdbc.Driver");
			this.url=props.getProperty("jdbc.url");
			this.username=props.getProperty("jdbc.username");
			this.password=props.getProperty("jdbc.password");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static ConnectionPool getInstance()
	{
		return connectionPool;
	}
	/**
	 *function:创建线程池
	 *preCondition:
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 *postConditeion:
	 *void
	 */
	public  static void createPool() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		if(findCurrentExitConnections()!=0)
			return;
		Driver driver=(Driver)Class.forName(jdbcdriver).newInstance();
		DriverManager.registerDriver(driver);
		createConnnections(initialConnections);
		
	}
	/**
	 *function:添加一组新的连接到连接池
	 *preCondition:
	 * @param num
	 * @throws SQLException
	 *postConditeion:
	 *void
	 */
	private static void createConnnections(int num) throws SQLException
	{
		for(int i=0;i<num;i++)
		{
			if(maxNumCOnnections>0&&findCurrentExitConnections()>maxNumCOnnections)
				break;
			unUsedConnections.add(createNewConnection());
		}
	}
	/**
	 *function:创建一个新的连接
	 *preCondition:
	 * @return
	 * @throws SQLException
	 *postConditeion:
	 *OneConnection 返回一个连接
	 */
	private static OneConnection createNewConnection() throws SQLException
	{
		
		Connection conn=DriverManager.getConnection(url, username, password); 
		//如果是第一次创建新的连接，首先判断一下数据库的最大的连接是否小于我们设置的最大连接，如果是，把当前最大
		//大连接设置为数据库的最大连接。当数据库的最大连接为0，表示不知道或是允许任意个。
		if(findCurrentExitConnections()==0)
		{
			DatabaseMetaData datameta=conn.getMetaData();
			int driverMaxConnections=datameta.getMaxConnections();
			if(driverMaxConnections>0&&maxNumCOnnections>driverMaxConnections)
				maxNumCOnnections=driverMaxConnections;
		}
		return new OneConnection(conn);
	}
	/**
	 *function: 获得当前线程池中已有的线程数
	 *preCondition:
	 * @return
	 *postConditeion:
	 *int  当前线程池中的线程数
	 */
	private static int findCurrentExitConnections()
	{
		return unUsedConnections.size()+usedConnections.size();
	}
	//获取连接
	public static synchronized Connection getConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		Connection conn=null;
		//如果连接池没有创建，创建连接池
		if(findCurrentExitConnections()==0)
			createPool();
		conn=getUnusedConnection();
		return conn;
	}
	//获得未用的数据库连接
	private static Connection getUnusedConnection() throws SQLException
	{
		//如果可用的连接没有了，创建新的连接
		if(getUnusedConnectionsSize()==0)
			createConnnections(incrementalConnections);
		OneConnection oneConn=unUsedConnections.poll();
		//验证连接是否可用，不可用重新创建一个
		if(!oneConn.getConnection().isValid(1000))
			oneConn=createNewConnection();
		usedConnections.add(oneConn);
		return oneConn.getConnection();
	}
	//把连接返回连接池
	public static synchronized void returnConnectionToPool(Connection conn) throws SQLException
	{
		//首先判断用户有没有手动关了连接，关了就不放入连接池，
		//这个方法有问题，以后修改
		if(conn.isClosed())
			return;
		else
		{
			Iterator iter=usedConnections.iterator();
			while(iter.hasNext())
			{
				if(conn==((OneConnection)iter.next()).getConnection())
				{
					iter.remove();
					break;
				}
			}
			unUsedConnections.add(new OneConnection(conn));
		}
	}
	//关闭连接池
	public static void closeConnectionsPool() throws Throwable
	{
		//关闭被用的数据库连接
		Iterator iter=usedConnections.iterator();
		OneConnection pConn=null;
		while(iter.hasNext())
		{
			wait(4000);
			pConn=(OneConnection)iter.next();
			closeConnection(pConn.getConnection());
			usedConnections.remove(pConn);
		}
		//关闭没用的数据库连接
		iter=unUsedConnections.iterator();
		while(iter.hasNext())
		{
			pConn=(OneConnection)iter.next();
			closeConnection(pConn.getConnection());
			unUsedConnections.remove(pConn);
		}
	}
	//等待时间
	private static void wait(int msecond)
	{
		try {
			Thread.sleep(msecond);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//关闭数据库连接
	private static void closeConnection(Connection conn) throws Throwable
	{
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("数据库连接关闭错误"+e.getMessage());
			Throwable se=new SQLException("数据库连接关闭错误");
			se.initCause(e);
			throw se;
		}
	}
	public static int getInitialConnections() {
		return initialConnections;
	}
	public static void setInitialConnections(int initialConnections) {
		ConnectionPool.initialConnections = initialConnections;
	}
	public static int getMaxNumCOnnections() {
		return maxNumCOnnections;
	}
	public static void setMaxNumCOnnections(int maxNumCOnnections) {
		ConnectionPool.maxNumCOnnections = maxNumCOnnections;
	}
	public static int getIncrementalConnections() {
		return incrementalConnections;
	}
	public static void setIncrementalConnections(int incrementalConnections) {
		ConnectionPool.incrementalConnections = incrementalConnections;
	}
	//返回连接池中的未用连接数
	public static int getUnusedConnectionsSize()
	{
		return unUsedConnections.size();
	}
	//返回连接池中的已用连接数
	public static int getUsedConnectionsSize()
	{
		return usedConnections.size();
	}
	
	/**
	 *function:
	 *preCondition:
	 * @param args
	 *postConditeion:
	 *void
	 */
	public static void main(String[] args) {
		try {
			Connection conn=ConnectionPool.getInstance().getConnection();
			Statement stat=conn.createStatement();
			ResultSet res=stat.executeQuery("select * from traininformation");
			ResultSetMetaData resd=res.getMetaData();
			int count=resd.getColumnCount();
			for(int i=1;i<=count;i++)
			{
				System.out.print(resd.getColumnName(i)+" ");
			}
			System.out.println();
			while(res.next())
			{
				for(int i=1;i<=count;i++)
				{
					System.out.print(res.getString(i)+" ");
				}
				System.out.println();
			}
			ConnectionPool.getInstance().returnConnectionToPool(conn);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
//		String jdbcDriver="com.mysql.jdbc.Driver";
//		String url="jdbc:mysql://localhost:3306/train_ticket";
//		String username="root";
//		String password="my8881234";
//		ConnectionPool cPoll=new ConnectionPool(jdbcDriver,url,username,password);
//		try {
//			long pre=System.currentTimeMillis();
//			for(int i=0;i<100;i++)
//			{
//				Connection conn=cPoll.getConnection();
//				Statement stat=conn.createStatement();
//				ResultSet rs=stat.executeQuery("select * from accessory");
//				while(rs.next())
//				{
//					//System.out.println(rs.getString(1)+","+rs.getString(2));
//				}
//				cPoll.returnConnectionToPool(conn);
//			}
//			long post=System.currentTimeMillis();
//			System.out.println(post-pre);
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	

}

class OneConnection

{
	private Connection conn;
	public Connection getConnection() {
		return conn;
	}
	public void setConnection(Connection conn) {
		this.conn = conn;
	}
	public OneConnection(Connection conn)
	{
		this.conn=conn;
		
	}
}
