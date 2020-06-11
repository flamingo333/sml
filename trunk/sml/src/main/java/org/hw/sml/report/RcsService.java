package org.hw.sml.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hw.sml.core.IfIdNotException;
import org.hw.sml.model.Result;
import org.hw.sml.report.model.Constants;
import org.hw.sml.report.model.ImportSql;
import org.hw.sml.report.model.ParamCriteria;
import org.hw.sml.report.model.ParamCriteriaForUpdate;
import org.hw.sml.report.model.PiTable;
import org.hw.sml.report.model.PiTableDetail;
import org.hw.sml.report.model.QuerySql;
import org.hw.sml.report.model.Queryer;
import org.hw.sml.report.model.UpdateSql;
import org.hw.sml.support.CallableHelper;
import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.Source;
import org.hw.sml.tools.DbTools;
import org.hw.sml.tools.MapUtils;

public abstract class RcsService extends Source{
	public static final  String CACHE_PRE="report";
	public abstract List<PiTableDetail> findAllByTableId(String id);
	public abstract PiTable get(String id);
	public int clear(String id){
		return getCacheManager().clearKeyStart(CACHE_PRE+":"+id+":");
	}
	public int clearQuery(String id){
		//return getCacheManager().clearKeyStart(CACHE_PRE+":"+id+":query");
		return 0;
	}
	private PiTable get0(String id){
		String key=CACHE_PRE+":"+id+":getPiTable";
		Object obj=getCacheManager().get(key);
		if(obj==null){
			obj=get(id);
			if(obj==null){
				throw new IfIdNotException("id "+id+" not find!");
			}
			getCacheManager().set(key,obj,-1);
		}
		return (PiTable)obj;
	}
	public  List<PiTableDetail> findAllByTableId0(String id){
		String key=CACHE_PRE+":"+id+":findAllByTableId";
		Object obj=getCacheManager().get(key);
		if(obj==null){
			obj=findAllByTableId(id);
			if(obj==null){
				throw new IfIdNotException("id "+id+" not find!");
			}
			getCacheManager().set(key,obj,-1);
		}
		return (List<PiTableDetail>)obj;
	}
	//----原始操作---开始
		public List<Map<String, Object>> query(String dbid,String sql, Object[] array,boolean inLog) {
			if(!inLog)
			LoggerHelper.getLogger().debug(getClass(),"query for sql["+sql+"],params["+((array==null||array.length==0)?"":Arrays.asList(array).toString())+"]");
			return getJdbc(dbid).queryForList(sql,array);
		}
		
		public List<Map<String, Object>> query(String dbid,String sql, Object[] array) {
			return query(dbid, sql, array, false);
		}
		public Long count(String dbid,String tableId,String sql, Object[] params) {
			sql="select count(1) from("+sql+") t1";
			//String key=new StringBuffer(CACHE_PRE+":"+dbid+"."+tableId+":"+"query"+":"+(sql+Arrays.asList(params)).hashCode()).toString();
			/*if(getCacheManager().get(key)!=null){
				return (Long)getCacheManager().get(key);
			}*/
			Long count=getJdbc(dbid).queryForObject(sql,params,Long.class);
			//if(count!=null&&count!=0)
			//getCacheManager().set(key,count,120);//缓存一小时
			return count;
		}
		public int update(String dbid,String sql, Object[] array,boolean inLog) {
			if(!inLog)
			LoggerHelper.getLogger().info(getClass(),"update for sql["+sql+"],params["+((array==null||array.length==0)?"":Arrays.asList(array).toString())+"]");
			return getJdbc(dbid).update(sql,array);
		}
		public int update(String dbid,String sql, Object[] array) {
			return update(dbid, sql, array,false);
		}
		
		public int updates(String dbid,final String sql,final List<Object[]> arrays){
			LoggerHelper.getLogger().info(getClass(),"updates for sql["+sql+"] sizes="+arrays.size());
			final int total=arrays.size();
			int count=0;
			int perOneSize=10000;
			int eachs=total/perOneSize+1;
			for(int j=0;j<eachs;j++){
				int start=j*perOneSize;
				int end=(j+1)*perOneSize>total?total:(j+1)*perOneSize;
				final List<Object[]> newArrays=arrays.subList(start,end);
				int[] is= getJdbc(dbid).batchUpdate(sql,newArrays);
				for(int i:is){
					count+=i;
				}
				LoggerHelper.getLogger().debug(getClass(),"updates for sql["+sql+"] start["+start+"]-----end["+end+"]");
			}
			return count;
		}
		//----原始操作结束
		
		public QuerySql getQuerySql(String id) {
			PiTable piTable=get0(id);
			List<PiTableDetail> piTableDetails=findAllByTableId0(id);
			return new QuerySql(piTable,piTableDetails);
		}
		
		public QuerySql getQuerySql(String id, ParamCriteria pc) {
			PiTable piTable=get0(id);
			pc.setSqlType(DbTools.getDbType(getJdbc(piTable.getDbId()).getDataSource()).name());
			List<PiTableDetail> piTableDetails=findAllByTableId0(id);
			return new QuerySql(piTable,piTableDetails,pc);
		}
		//更新操作
		public int update(String id, ParamCriteriaForUpdate pcu) {
			PiTable piTable=get0(id);
			List<PiTableDetail> piTableDetails=findAllByTableId0(id);
			UpdateSql updateSql=new UpdateSql(pcu,piTable,piTableDetails);
			return update(piTable.getDbId(),updateSql.toString(),updateSql.getUpdateParams().toArray(new Object[]{}),pcu.getInLog());
		}
		//查询
		public List<Map<String,Object>>  query(String id, ParamCriteria pc) {
			QuerySql querySql=getQuerySql(id,pc);
			return query(querySql.getPiTable().getDbId(),querySql.toString(),querySql.getQueryParam().toArray(new Object[]{}));
		}
		public List<Map<String,Object>> query(Queryer queryer){
			List<Map<String,Object>> datas=query(queryer.getRcptId(),queryer.getParamCriteria());
			return toCaseForKey(datas, queryer.getToCaseForKey());
		}
		//查询
		public Result getResult(Queryer queryer){
			Result result= getResult(queryer.getRcptId(),queryer.getParamCriteria());
			result.setDatas(toCaseForKey(result.getDatas(),queryer.getToCaseForKey()));
			return result;
		}
		private List<Map<String,Object>> toCaseForKey(List<Map<String,Object>> datas,String caseK){
			if(caseK!=null&&Arrays.asList("upper","lower").contains(caseK)){
				return MapUtils.toCaseForKey(datas,caseK);
			}
			return datas;
		}
		public List<Map<String,Object>> queryChart(Queryer queryer){
			QuerySql querySql=getQuerySql(queryer.getRcptId(),queryer.getParamCriteria());
			List<Map<String,Object>> datas= query(querySql.getPiTable().getDbId(),querySql.getChartSql(),querySql.getQueryParamWithOutPage().toArray(new Object[]{}));
			return toCaseForKey(datas, queryer.getToCaseForKey());
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Result getResult(final String id, final ParamCriteria pc) {
			final QuerySql querySql=getQuerySql(id,pc);
			Result result=new Result();
			List rs=CallableHelper.callresults(new Callable() {
				public Object call() throws Exception {
					return count(querySql.getPiTable().getDbId(),id,querySql.getQuerySql(),querySql.getQueryParamWithOutPage().toArray(new Object[]{}));
				}
			},new Callable(){
				public Object call() throws Exception {
					//缓存第一页
					if(pc.getPage()==1&&pc.getRowPerPage()<=50){
						List value=null;
						/*String key=CACHE_PRE+":"+id+":query:"+(querySql.toString()+querySql.getQueryParam()).hashCode();
						List value=(List)getCacheManager().get(key);
						if(value!=null){
							return value;
						}else{*/
							value=query(querySql.getPiTable().getDbId(),querySql.toString(),querySql.getQueryParam().toArray(new Object[]{}));
							//if(value.size()>0)
							//getCacheManager().set(key, value,120);
							return value;
						//}
					}else
					return query(querySql.getPiTable().getDbId(),querySql.toString(),querySql.getQueryParam().toArray(new Object[]{}));
				}
			});
			result.setCount(Long.parseLong(String.valueOf(rs.get(0)==null?0:rs.get(0))));
			result.setDatas((List)(rs.get(1)==null?new ArrayList():rs.get(1)));
			result.setPage(pc.getPage());
			result.setLimit(pc.getRowPerPage());
			return result;
		}
		//导入根据id导入默认模板2007xlsx
		public int importReport(String id, String type,
				List<Map<String,Object>> datas) {
			int i=0;
			PiTable piTable=get0(id);
			List<PiTableDetail> piTableDetails=findAllByTableId0(id);
			ImportSql importSql=new ImportSql(type, piTable, piTableDetails, datas);
			List<UpdateSql> updateSqls=importSql.getUpdateSqls();
			if(type.equals(Constants.TYPE_ADU)){
				
			}
			//---
			if(updateSqls.size()==0){
				return 0;
			}
			String dbid=piTable.getDbId();
			String sql=updateSqls.get(0).toString();
			List<Object[]> arrays=new ArrayList<Object[]>();
			for(UpdateSql updateSql:updateSqls){
				arrays.add(updateSql.getUpdateParams().toArray());
			}
			i=updates(dbid, sql, arrays);
			return i;
		}
}
