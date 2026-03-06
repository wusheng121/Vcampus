package util;

public enum collegeStructure {

	WULI("物理学院",0),
	SHUXUE("数学学院",1),
	JISUANJI("计算机学院",2),
	HUAXUE("化学学院",3),
	YISHU("艺术学院",4),
	SHANG("商学院",5);
	
	private String name;
	private int index;
	private collegeStructure(String name, int index) {
		this.name=name;
		this.index=index;
		
	}
	public static final String [][] major = {
			
			{"光学","电学","原子物理","核物理"},
			{"数学与应用数学","大数据科学","理论数学"},
			{"软件工程","人工智能","计算机科学工程"},
			{"分析化学","理论化学","化学与应用化学","化工"},
			{"编导","美术","声乐","艺术设计"},
			{"工商管理","经济学","金融学"},
	};
	public static final String [][] maJorNum = {
			{"901","902","903","904"},
			{"801","802","803"},
			{"701","702","703"},
			{"601","602","603","604"},
			{"501","502","503","504"},
			{"401","402","403"},
	};
	public static final String []sex = {"男","女"};
	
	public static final String [] secondaryStr = {
			WULI.getName(),
			SHUXUE.getName(),
			JISUANJI.getName(),
			HUAXUE.getName(),
			YISHU.getName(),
			SHANG.getName(),
	};
	
	public String getName() {
		return name;
	}
	public int getIndex() {
		return index;
	}
	public static String[][] getMajor() {
		return major;
	}
	public static String[][] getMajornum() {
		return maJorNum;
	}
	public static String[] getGradestr() {
		return sex;
	}
	
}
