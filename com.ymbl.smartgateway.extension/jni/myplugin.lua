#!/tmp/transite-target/bin/lua
os.execute("/tmp/transite-target/bin/xl2tpd -c /tmp/transite-target/etc/xl2tpd.conf")
os.execute("echo \"c xbspeed\" > /tmp/transite-target/var/run/l2tp-control")
os.execute("ip route add 172.18.0.0/16 via 172.18.0.1 table 252")
