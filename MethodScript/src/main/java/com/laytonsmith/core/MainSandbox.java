package com.laytonsmith.core;

/**
 * This class is for testing concepts
 */
public class MainSandbox {

	public static void main(String[] argv) throws Exception {	
//		URL url = ClassDiscovery.GetClassContainer(MainSandbox.class);
//		long start;
//		start = System.currentTimeMillis();
//		ClassDiscoveryURLCache cdc = new ClassDiscoveryURLCache(url);
//		StreamUtils.GetSystemOut().println((System.currentTimeMillis() - start) + "ms for first one");
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		cdc.writeDescriptor(baos);
//		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//		start = System.currentTimeMillis();
//		cdc = new ClassDiscoveryURLCache(url, bais);
//		StreamUtils.GetSystemOut().println((System.currentTimeMillis() - start) + "ms for second one");
//		StreamUtils.GetSystemOut().println(StringUtils.HumanReadableByteCount(baos.size()));
//		baos = new ByteArrayOutputStream();
//		ZipOutputStream zip = new ZipOutputStream(baos);
//		zip.putNextEntry(new ZipEntry("root"));
//		cdc.writeDescriptor(zip);
//		zip.close();
//		StreamUtils.GetSystemOut().println(StringUtils.HumanReadableByteCount(baos.toByteArray().length));
		
//		//URI information
//		String[] uris = new String[]{"yml:user@remote:22:abcd:path/to/remote/file"
//		,"yml:user@remote:22:/path/to/remote/file", "yml:user@remote:/path/to/remote/file",
//		"sqlite://../../file.db?query"};
//		for (String s : uris) {
//			java.net.URI uri = new java.net.URI(s);
//			StreamUtils.GetSystemOut().println("For the URI " + uri.toString() + ", the following are set:");
//			StreamUtils.GetSystemOut().println("Scheme: " + uri.getScheme());
//			StreamUtils.GetSystemOut().println("Scheme specific part: " + uri.getSchemeSpecificPart());
//			StreamUtils.GetSystemOut().println("Authority: " + uri.getAuthority());
//			StreamUtils.GetSystemOut().println("User info: " + uri.getUserInfo());
//			StreamUtils.GetSystemOut().println("Host: " + uri.getHost());
//			StreamUtils.GetSystemOut().println("Port: " + uri.getPort());
//			StreamUtils.GetSystemOut().println("Path: " + uri.getPath());
//			StreamUtils.GetSystemOut().println("Query: " + uri.getQuery());
//			StreamUtils.GetSystemOut().println("Fragment: " + uri.getFragment());
//			StreamUtils.GetSystemOut().println("\n\n***********************************\n\n");
//		}
		
		//Execution queue usage
//		final ExecutionQueue queue = new ExecutionQueue("Test", "default");
//		for(int i = 0; i < 3; i++){
//			final int j = i;
//			new Thread(new Runnable() {
//
//				public void run() {
//					for(int i = 0; i < 10; i++){
//						final int k = i;
//						queue.push("queue-" + j, new Runnable(){
//
//							public void run() {
//								String space = "";
//								switch(j){
//									case 1: space = "     "; break;
//									case 2: space = "          "; break;
//								}
//								StreamUtils.GetSystemErr().println("In Queue " + space + j + ": " + k);
//							}
//						});
//					}
//					StreamUtils.GetSystemErr().println("Finished queueing up events for queue " + j);
//				}
//			}, "Thread-" + i).start();
//		}
		
		//Profiler usage
//		Profiler p = new Profiler(new File("profiler.config"));
//		p.doLog("Starting profiling");
//		ProfilePoint ProfilerTop = p.start("Profiler Top", LogLevel.ERROR);
//		ProfilePoint ProfilerMiddle = p.start("Profiler Middle", LogLevel.WARNING);
//		ProfilePoint ProfilerBottom = p.start("Profiler Bottom", LogLevel.INFO);
//		ProfilePoint InnerMost1 = p.start("InnerMost1", LogLevel.DEBUG);
//		ProfilePoint InnerMost2 = p.start("InnerMost2", LogLevel.VERBOSE);
////		System.gc();
//		p.stop(InnerMost2);
//		p.stop(InnerMost1);
//		p.stop(ProfilerBottom);
//		p.stop(ProfilerMiddle);
//		p.stop(ProfilerTop);
		
		//Profiling reg_split vs split
//		String toSplit = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z";
//		Regex.reg_split regSplitFunction = new Regex.reg_split();
//		StringHandling.split splitFunction = new StringHandling.split();
//		Target t = Target.UNKNOWN;
//		int times = 1000000;
//		
//		CString subject = new CString(toSplit, t);
//		CString pattern = new CString(",", t);
//		
//		long startRegSplit = System.currentTimeMillis();
//		{
//			for(int i = 0; i < times; i++){
//				regSplitFunction.exec(t, null, pattern, subject);
//			}
//		}
//		long stopRegSplit = System.currentTimeMillis();
//		long startSplit = System.currentTimeMillis();
//		{
//			for(int i = 0; i < times; i++){
//				splitFunction.exec(t, null, subject, pattern);
//			}
//		}
//		long stopSplit = System.currentTimeMillis();
//		
//		StreamUtils.GetSystemOut().println("reg_split took " + (stopRegSplit - startRegSplit) + "ms under " + times + " iterations.");
//		StreamUtils.GetSystemOut().println("split took " + (stopSplit - startSplit) + "ms under " + times + " iterations.");
//		InetAddress i1 = InetAddress.getByName("173.194.37.72");
//		StreamUtils.GetSystemOut().println("i1.toString(): " + i1.toString());
//		StreamUtils.GetSystemOut().println("i1.getHostAddress(): " + i1.getHostAddress());
//		InetAddress i2 = InetAddress.getByName("173.194.37.72");
//		StreamUtils.GetSystemOut().println("i2.toString(): " + i2.toString());
//		StreamUtils.GetSystemOut().println("i2.getHostAddress(): " + i2.getHostAddress());
//		StreamUtils.GetSystemOut().println("i2.getHostName(): " + i2.getHostName());
//		StreamUtils.GetSystemOut().println("i2.toString(): " + i2.toString());
//		StreamUtils.GetSystemOut().println("i2.getHostAddress(): " + i2.getHostAddress());
		
//		DB.CConnection conn = DB.CConnection.GetConnection(DB.SupportedDBConnectors.MYSQL, "localhost", "test", 3306, "", "");
//		DB db = new MySQLProfile();
//		db.connect(conn);
//		Object o = db.query("SELECT * FROM test;");
//		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?generateSimpleParameterMetadata=true");
//		PreparedStatement ps = conn.prepareStatement("SELECT * FROM test WHERE c1=?");
//		StreamUtils.GetSystemOut().println(ps.getParameterMetaData().getParameterType(1));
//		System.exit(0);
//		if(o instanceof ResultSet){
//			ResultSet rs = (ResultSet)o;
//			while(rs.next()){
//				ResultSetMetaData rsmd = rs.getMetaData();
//				StreamUtils.GetSystemOut().println(rsmd.getColumnCount());
//				for(int i = 1; i <= rsmd.getColumnCount(); i++){
//					StreamUtils.GetSystemOut().println(rsmd.getColumnName(i));
//				}
//			}
//		}
	}
	
	
}
