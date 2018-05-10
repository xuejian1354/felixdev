package com.ymbl.smartgateway.extension;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IpTables {

	private static IpTables iptables = null;

	private IpTables(){
		try {
			addLoadLibs(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static IpTables instance(){
		if (iptables == null) {
			iptables = new IpTables();
			System.out.println("IpTables instance");
		}

		return iptables;
	}

	public void addLoadLibs(boolean isreload) throws IOException {
		String[] xtables_lib_list = {
				"libip6t_DNAT.so", "libip6t_DNPT.so", "libip6t_HL.so", "libip6t_LOG.so", 
				"libip6t_MASQUERADE.so", "libip6t_NETMAP.so", "libip6t_REDIRECT.so", 
				"libip6t_REJECT.so", "libip6t_SNAT.so", "libip6t_SNPT.so", "libip6t_ah.so", 
				"libip6t_dst.so", "libip6t_eui64.so", "libip6t_frag.so", "libip6t_hbh.so", 
				"libip6t_hl.so", "libip6t_icmp6.so", "libip6t_ipv6header.so", "libip6t_mh.so", 
				"libip6t_rt.so", "libipt_CLUSTERIP.so", "libipt_DNAT.so", "libipt_ECN.so", 
				"libipt_LOG.so", "libipt_MASQUERADE.so", "libipt_MIRROR.so", "libipt_NETMAP.so",
				"libipt_REDIRECT.so", "libipt_REJECT.so", "libipt_SAME.so", "libipt_SNAT.so", 
				"libipt_TTL.so", "libipt_ULOG.so", "libipt_ah.so", "libipt_icmp.so", 
				"libipt_realm.so", "libipt_ttl.so", "libipt_unclean.so", "libxt_AUDIT.so", 
				"libxt_CHECKSUM.so", "libxt_CLASSIFY.so", "libxt_CONNMARK.so", 
				"libxt_CONNSECMARK.so", "libxt_CT.so", "libxt_DSCP.so", "libxt_HMARK.so",
				"libxt_IDLETIMER.so", "libxt_LED.so", "libxt_MARK.so", "libxt_NFLOG.so", 
				"libxt_NFQUEUE.so", "libxt_NOTRACK.so", "libxt_RATEEST.so", "libxt_SECMARK.so",
				"libxt_SET.so", "libxt_SYNPROXY.so", "libxt_TCPMSS.so", "libxt_TCPOPTSTRIP.so",
				"libxt_TEE.so", "libxt_TOS.so", "libxt_TPROXY.so", "libxt_TRACE.so",
				"libxt_addrtype.so", "libxt_bpf.so", "libxt_cluster.so", "libxt_comment.so", 
				"libxt_connbytes.so", "libxt_connlimit.so", "libxt_connmark.so", 
				"libxt_conntrack.so", "libxt_cpu.so", "libxt_dccp.so", "libxt_devgroup.so", 
				"libxt_dscp.so", "libxt_ecn.so", "libxt_esp.so", "libxt_hashlimit.so", 
				"libxt_helper.so", "libxt_iprange.so", "libxt_ipvs.so", "libxt_length.so",
				"libxt_limit.so", "libxt_mac.so", "libxt_mark.so", "libxt_multiport.so",
				"libxt_nfacct.so", "libxt_osf.so", "libxt_owner.so", "libxt_physdev.so", 
				"libxt_pkttype.so", "libxt_policy.so", "libxt_quota.so", "libxt_rateest.so",
				"libxt_recent.so", "libxt_rpfilter.so", "libxt_sctp.so", "libxt_set.so",
				"libxt_socket.so", "libxt_standard.so", "libxt_state.so", "libxt_statistic.so",
				"libxt_string.so", "libxt_tcp.so", "libxt_tcpmss.so", "libxt_time.so", 
				"libxt_tos.so", "libxt_u32.so", "libxt_udp.so"
		};

		loadLib("libip4tc.so.0", "/lib", false, false);
		loadLib("libip6tc.so.0", "/lib", false, false);
		loadLib("libxtables.so.10", "/lib", false, false);
		for (String xtlib : xtables_lib_list) {
			loadLib(xtlib, "/tmp/transite-target/lib/xtables", false, false);
		}

		loadLib("transite.so", "/tmp/transite-target/lib", true, isreload);
	}

	private synchronized static void loadLib(String libName, String targetDir, boolean fornative, boolean isreload) throws IOException {

		InputStream in = null;
		BufferedInputStream reader = null;
		FileOutputStream writer = null;  

		File ftd = new File(targetDir);
		if (ftd.isFile()) {
			ftd.delete();
		}

		if (!ftd.exists()) {
			ftd.mkdirs();
		}

		File extractedLibFile = new File(targetDir+"/"+libName);
		if (isreload && extractedLibFile.exists()) {
			extractedLibFile.delete();
		}

		if(!extractedLibFile.exists()) {
			try {
				in = IpTables.class.getResourceAsStream("/" + libName);
				if(in==null)
					in =  IpTables.class.getResourceAsStream(libName);
				reader = new BufferedInputStream(in);
				writer = new FileOutputStream(extractedLibFile);

				byte[] buffer = new byte[1024];
				while (reader.read(buffer) > 0) {
					writer.write(buffer);
					buffer = new byte[1024];
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(in!=null)
					in.close();
				if(writer!=null)
					writer.close();
			}
        }

		if (fornative) {
			System.load(extractedLibFile.toString());			
		}
	}

	public native byte[] getMacAddr(String dev);
	public native int rule(String rule);
}
