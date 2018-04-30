package org.hw.sml.test.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.hw.sml.support.aop.AbstractAspect;
import org.hw.sml.support.aop.Invocation;
import org.hw.sml.support.aop.JdkAop;
import org.hw.sml.support.aop.MethodProxyFactory;

public class AopTest {
	 public static interface IHelloWorld{
	    	String getName();
	    	String add(int i,int j);
	    	public String getAge();
	    }
	    public static class HellowWorld implements IHelloWorld{
	    	private String name="13";
	    	private String age="12";
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public String getAge() {
				return age;
			}
			public void setAge(String age) {
				this.age = age;
			}
			@Override
			public String add(int i, int j) {
				return i+j+"";
			}
			public String toString(){
				return this.name;
			}
	    	
	    }
	    interface Mapper{
	    	public String hello(String name);
	    }
	    public static void main(String[] args) {
	    	AbstractAspect aspect=new AbstractAspect() {
	    		{
	    			setPackageMatchs("org.hw.sml.test.(test|aop).*.(getName|getAge)");
	    		}
				public void doBefore(Invocation invocation)  throws Throwable{
					invocation.getExtInfo().put("start",System.currentTimeMillis());
				}
				public void doAfter(Invocation invocation)  throws Throwable{
					invocation.setValue(invocation.getValue()+" 拦截器1");
					Long start=(Long) invocation.getExtInfo().get("start");
					System.out.println("耗时："+(System.currentTimeMillis()-start));
				}
			};
			AbstractAspect aspect1=new AbstractAspect() {
	    		{
	    			setPackageMatchs("org.hw.sml.test1.(test|aop).*.(add) org.hw.sml.test.(test|aop).*.(add)");
	    		}
				public void doBefore(Invocation invocation)  throws Throwable{
					
				}
				public void doAfter(Invocation invocation)  throws Throwable{
					invocation.setValue(invocation.getValue()+" 拦截器2");
				}
			};
			IHelloWorld hw=MethodProxyFactory.newProxyInstance(new HellowWorld(),aspect,aspect1);
	    	System.out.println("add:"+hw.add(1, 2));
	    	System.out.println("getName:"+hw.getName());
	    	System.out.println("getAge:"+hw.getAge());
	    	System.out.println("toString:"+hw.toString());
	    	Mapper mapper=MethodProxyFactory.newInstance(Mapper.class, new InvocationHandler() {
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					return "hello "+args[0];
				}
			});
	    	System.out.println(mapper.hello("world"));
		}
}
