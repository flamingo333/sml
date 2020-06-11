package org.hw.sml.report;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.jdbc.RowMapper;
import org.hw.sml.report.model.PiTable;
import org.hw.sml.report.model.PiTableDetail;
/**
 * 
 */
public class ReportCommonService extends RcsService{
	public static final  String CACHE_PRE="report";
	public PiTable get(String id) {
		String key=CACHE_PRE+":"+id+":getPiTable";
		if(getCacheManager().get(key)!=null){
			return (PiTable)getCacheManager().get(key);
		}
		PiTable result=getJdbc("defJt").queryForObject(FrameworkConstant.getSupportKey(frameworkMark,"CFG_REPORT_SQL"),new Object[]{id},new RowMapper<PiTable>(){
			public PiTable mapRow(ResultSet rs, int arg1) throws SQLException {
				PiTable pi=new PiTable();
				pi.setId(rs.getString("id"));
				pi.setTableName(rs.getString("tablename"));
				pi.setDescription(rs.getString("description"));
				pi.setDbId(rs.getString("db_id"));
				return pi;
			}
		});
		if(result!=null)
			getCacheManager().set(key, result,-1);
		return result;
	}
	public List<PiTableDetail> findAllByTableId(String id){
		String key=CACHE_PRE+":"+id+":findAllByTableId";
		if(getCacheManager().get(key)!=null){
			return (List<PiTableDetail>)getCacheManager().get(key);
		}
		List<PiTableDetail> result= getJdbc("defJt").query(FrameworkConstant.getSupportKey(frameworkMark,"CFG_REPORT_DETAIL_SQL"),new Object[]{id},new RowMapper<PiTableDetail>(){
			public PiTableDetail mapRow(ResultSet rs, int arg1) throws SQLException {
				PiTableDetail pi=new PiTableDetail();
				pi.setTableId(rs.getString("table_id"));
				pi.setField(rs.getString("field_name"));
				pi.setFieldType(rs.getString("field_Type"));
				pi.setFieldZn(rs.getString("field_name_zn"));
				pi.setFormat(rs.getString("format"));
				pi.setLength(rs.getString("length"));
				pi.setOrderIndex(rs.getInt("order_index"));
				pi.setForImport(rs.getInt("for_import"));
				pi.setForUpdate(rs.getInt("for_update"));
				pi.setForInsert(rs.getInt("for_insert"));
				pi.setForImportUpdate(rs.getInt("for_import_update"));
				pi.setForQuery(rs.getInt("for_query"));
				pi.setIsQuery(rs.getInt("is_query"));
				return pi;
			}
		});
		if(result!=null&&!result.isEmpty())
		getCacheManager().set(key,result,-1);
		return result;
	}
}
