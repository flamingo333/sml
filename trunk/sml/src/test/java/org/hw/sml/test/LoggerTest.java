package org.hw.sml.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.hw.sml.tools.Https;
import org.hw.sml.tools.IOUtils;

public class LoggerTest {
	
	public static void main(String[] args) throws Exception {
		String strs="gsm_teletraffic,gsm_wireless_insert_ratio,gsm_flow_all,lte_flow_sgi,lte_wire_conn_ratio,lte_wire_drop_ratio,low_join_cell_cnt,high_drop_cell_cnt,high_prb_ul_util_cell_cnt,high_prb_dl_util_cell_cnt,mme_sub_nbrsub,msc_subscrib_in_vlr,volte_voice_teletraffic,volte_voice_conn_ratio,volte_voice_drop_ratio,volte_scscf_voice_teletraffic,volte_scscf_register_user_cnt,volte_sbc_voice_teletraffic,volte_sbc_register_user_cnt,message_conn_ratio,message_flow,mms_send_num,volte_wireless_drop_ratio_cell,volte_wireless_drop_ratio,volte_video_teletraffic,lte_flow,lte_ul_prb_use_ratio,lte_dl_prb_use_ratio,lte_nouser_att_succ_ratio,lte_tau_succ_ratio,lte_sw_succ_ratio,lte_ul_prb_use_ratio_fz,lte_ul_prb_use_ratio_fm,lte_dl_prb_use_ratio_fz,lte_dl_prb_use_ratio_fm,lte_nouser_att_succ_ratio_fz,lte_nouser_att_succ_ratio_fm,lte_tau_succ_ratio_fz,lte_tau_succ_ratio_fm,lte_wire_conn_ratio_fz1,lte_wire_conn_ratio_fm1,lte_wire_conn_ratio_fz2,lte_wire_conn_ratio_fm2,lte_wire_drop_ratio_fz,lte_wire_drop_ratio_fm,lte_sw_succ_ratio_fz,lte_sw_succ_ratio_fm,volte_wireless_conn_ratio,esrvcc_sw_succ_ratio,volte_wireless_conn_ratio_fz1,volte_wireless_conn_ratio_fm1,volte_wireless_conn_ratio_fz2,volte_wireless_conn_ratio_fm2,volte_wireless_drop_ratio_fz,volte_wireless_drop_ratio_fm,esrvcc_sw_succ_ratio_fz,esrvcc_sw_succ_ratio_fm,volte_voice_conn_ratio_fz1,volte_voice_conn_ratio_fm1,volte_voice_conn_ratio_fz2,volte_voice_conn_ratio_fm2,total_teletraffic,total_flow,peak_flow_rate,peak_bandwidth_utilization,http_succ_ratio,http_load_time,game_delay,game_packet_loss_ratio,video_jammed_num,video_first_frame_time,video_avg_buffer_ratio,ott_01_peak_flow_rate,ott_01_peak_bw_utilization,csfb_called_bp_succ_ratio,csfb_called_fall_succ_ratio,volte_net_conn_succ_ratio,gsm_voice_sw_drop_ratio,gsm_voice_wl_drop_ratio,gsm_highflow_cell_ratio,net_conn_succ_ratio,mms_ptp_succ_ratio,csfb_called_bp_succ_ratio_fz,csfb_called_bp_succ_ratio_fm,csfb_called_fall_succ_ratio_fz,csfb_called_fall_succ_ratio_fm,volte_net_conn_succ_ratio_fz,volte_net_conn_succ_ratio_fm,gsm_voice_sw_drop_ratio_fz,gsm_voice_sw_drop_ratio_fm,gsm_voice_wl_drop_ratio_fz,gsm_voice_wl_drop_ratio_fm,net_conn_succ_ratio_fz,net_conn_succ_ratio_fm,mms_ptp_succ_ratio_fz,mms_ptp_succ_ratio_fm,gsm_wireless_use_ratio,gsm_wireless_use_ratio_fz,gsm_wireless_use_ratio_fm,gsm_voice_access_ratio,gsm_voice_access_ratio_fz1,gsm_voice_access_ratio_fm1,gsm_voice_access_ratio_fz2,gsm_voice_access_ratio_fm2,gsm_tchhalf_ratio,gsm_tchhalf_ratio_fz,gsm_tchhalf_ratio_fm,prov_roam_in_user_num,intl_roam_in_user_num,prov_roam_out_user_num,intl_roam_out_user_num,iot_user_num,broadband_user_num,peak_user_num,peak_address_poll_use_ratio,webtv_user_num,peak_user_num_stb,mobile_lte_residing_ratio_d,mobile_lte_residing_ratio_m,lte_bad_cell_ratio,total_lte_cell_cnt,lte_site_out_ratio,gsm_bad_cell_ratio,gsm_highdrop_cell_cnt,gsm_highjam_cell_cnt,gsm_highdropandjam_cell_cnt,total_gsm_cell_cnt,gsm_site_out_ratio";
		int i=0;
		for(String str:strs.split(",")){
			System.out.println(i++ +"   "+str.length() +"     ->"+str);
			//System.out.print(str +" as "+str+"_,");
		}
	}
	public static String toString(InputStream is, String charset)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(baos, is);
		return baos.toString(charset);
	}
	public static void copy(OutputStream os,InputStream is) throws IOException{
		byte[] bs = new byte[512];
		int temp = -1;
		while ((temp = is.read(bs)) != -1) {
			os.write(bs, 0, temp);
		}
	}
	public static class R implements Runnable{
		static int count=0;
		public void run() {
			try{
					Https https=Https.newGetHttps("https://183.207.101.110:56789/api/report")
							.registerTrust()//注册证书
							.basicAuth("eastcom-sw:t2h3i5n#")//认证
							.param("request",IOUtils.toString("d:/t.txt", "utf8"))//参数
							//.bos(new FileOutputStream("d:/tt.txt"))//返回内容写入文件
							;
					System.out.println(https.execute());//执行
				}catch(Exception e){
					e.printStackTrace();
				}
			
		}
		
	}
}
