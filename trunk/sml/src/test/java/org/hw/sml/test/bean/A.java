package org.hw.sml.test.bean;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hw.sml.core.SqlMarkupTemplate;
import org.hw.sml.support.el.ElException;
import org.hw.sml.support.el.SmlElContext;
import org.hw.sml.support.ioc.BeanHelper;
import org.hw.sml.support.ioc.annotation.Val;
import org.hw.sml.support.time.annotation.Scheduler;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.MapUtils;

import com.alibaba.fastjson.JSON;

public class A {
	private String str;
	private  Object i;
	private Character c;
	private Double d;
	private Long l;
	private Short s;
	private Float f;
	private Boolean flag;
	private int count;
	public A(int i){
		this.i=i;
	}
	public A(){
		
	}
	@Val("{a:#{person.age},b:${properties}}")
	public String get(String a,String b,String c){
		return a+b+c;
	}
	public String get(String ... strs){
		return Arrays.asList(strs).toString();
	}
	@Scheduler(Scheduler.min1)
	public void test4() throws ElException{
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(new A().i.toString());
	}
	@Scheduler(Scheduler.min1)
	public void test2() throws ElException{
		List<Object[]> datas=MapUtils.newArrayList();
		datas.add(new Object[]{"1","2"});
		datas.add(new Object[]{"1","2"});
		System.out.println(BeanHelper.getBean(SqlMarkupTemplate.class).getJdbc("rest").batchUpdate("insert into hw_test(id,name) values(?,?)",datas).length);
		if(count++==1){
			BeanHelper.evelV("#{schedulerPanner.taskMapStatus.put(('anno-aBean.test'),true)}");
		}
		System.out.println(JSON.toJSON(BeanHelper.evelV("#{schedulerPanner.stats}")));
		System.out.println("=-----="+BeanHelper.evelV("#{sml.getDefJt().queryForList('select * from dual where 1<? and 'a'||'c'=?',(#{([2,'ace']).toArray()}))}"));
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Scheduler(Scheduler.min1)
	public void test3(){
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void test(long i){
		
	}
	public A(String str, Object i, char c, double d, long l, short s, Float f, Boolean flag) {
		super();
		this.str = str;
		this.i = i;
		this.c = c;
		this.d = d;
		this.l = l;
		this.s = s;
		this.f = f;
		this.flag = flag;
	}
	@Override
	public String toString() {
		return "A [str=" + str + ", i=" + i + ", c=" + c + ", d=" + d + ", l=" + l + ", s=" + s + ", f=" + f + ", flag="
				+ flag + "]";
	}
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, ElException {
		System.out.println(new Object().getClass().isAssignableFrom(String.class));
		BeanHelper.start();
		//System.out.println(BeanHelper.getBean(PropertiesHelper.class).getValues());
		long start=System.currentTimeMillis();
		System.out.println(Arrays.asList(A.class.getConstructors()[1].getParameterTypes()));
		System.out.println(BeanHelper.getBean(A.class));
		System.out.println(BeanHelper.evelV("12.0d"));
		System.out.println(BeanHelper.evelV("#{hi.length}"));
		System.out.println(BeanHelper.evelV("#{mk.get(2i)}"));
		System.out.println(BeanHelper.evelV("#{aBean.get('1','2','3')}"));
		System.out.println(JSON.toJSONString(BeanHelper.evelV("#{hi[1]}")));
		System.out.println(BeanHelper.evelV("#{aBean.get(#{mark})}"));
		BeanHelper.evelV("#{sml.cacheManager.set('hlw',123,1i)}");
		//System.out.println(sml.getCacheManager().getKeyStart(""));
		System.out.println(BeanHelper.evelV("#{sml.cacheManager.getKeyStart('')}"));
		//System.out.println(BeanHelper.evelV("#{sml.smlContextUtils.query(\"area-pm\",\"\")}"));
		System.out.println(BeanHelper.evelV("#{sml.cacheManager.clear()}"));
		System.out.println(BeanHelper.evelV("${username}"));
		System.out.println(BeanHelper.evelV("#{sml.cacheManager.getKeyStart('')}"));
		System.out.println(BeanHelper.evelV("#{sml.getJdbc('defJt').dataSource.toString().equals(#{sml.getJdbc('defJt').dataSource.toString()})}"));
		System.out.println(BeanHelper.evelV("'aaaad,dd,d'"));
		System.out.println("--------------"+BeanHelper.evelV("{a:{a:2,c:3},b:'eeeee',c:(['a','b','c','d',({a:({a:2,c:3,d:({f:1,g:2,e:({e:1})})}),b:'eeeee'})])}"));
		System.out.println(BeanHelper.evelV("#{(['a','b','c','d',{a:{a:2,c:3},b:'eeeee'}]).containsAll((['a','b']))}"));
		System.out.println(BeanHelper.evelV("#{('a').length().toString().concat(('dess,sss')).equals(('1dess,sss'))}"));
		System.out.println(BeanHelper.evelV("#{([1]).toArray()}"));
		System.out.println("=-----="+BeanHelper.evelV("#{sml.getJdbc('rest').queryForList('select * from dual where 1<? and 'a'||'c'=?',(#{([2,'ac']).toArray()}))}"));
		Map map=(Map) BeanHelper.evelV("{'a':1,'b':2,'c':({234i:'3','e':'4','f':({'g':'6'})})}");
		System.out.println(map);
		System.out.println(BeanHelper.evelV("{a:({b:1,c:   ({d:1})   })}"));
		List<Map> list=(List<Map>) BeanHelper.evelV("([({a:1,b:2,c:3}),({a:2,b:3,c:2}),({a:3,b:3,c:1})])");
		BeanHelper.getBean(SmlElContext.class).withBean("testMap",list);
		System.out.println(BeanHelper.evelV("#{testMap}").getClass());
		System.out.println(BeanHelper.evelV("#{smlMapHelper.sort(#{testMap},'a','desc')}"));
		System.out.println(BeanHelper.evelV("#{('k').equals('k')}"));
		System.out.println(BeanHelper.evelV("{1:2,a:b,c:d,e:f}"));
		System.out.println(BeanHelper.evelV("#{(128+(2-9)*10+900+(10*97)+128*23/4)}"));
		System.out.println(System.currentTimeMillis()-start);
		//ArrayList c;
		//"".matches(regex)
		//"".equals(anObject)
		Boolean flag=new Object().getClass().isAssignableFrom(Integer.class);
		System.out.println(flag);
		System.out.println(BeanHelper.evelV("#{schedulerPanner.getTaskMapStatus().put('anno-aBean.test',false)}"));
		System.out.println(BeanHelper.evelV("#{schedulerPanner.getTaskMapStatus()}"));
		Map<String,String> m=new HashMap<String,String>();
		String key=m.put("a","b");
		key=m.put("a","c");
		System.out.println(key);
		System.out.println(A.class.getInterfaces().length);
		System.out.println(Arrays.asList(ClassUtil.getInterfaces(A.class)));
		List<Object[]> datas=MapUtils.newArrayList();
		datas.add(new Object[]{"1","2"});
		datas.add(new Object[]{"1","2"});
		System.out.println(BeanHelper.getBean(SqlMarkupTemplate.class).getJdbc("rest").batchUpdate("insert into hw_test(id,name) values(?,?)",datas).length);

	}


}
