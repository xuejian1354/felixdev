#!/tmp/transite-target/bin/lua
os.execute("killall -9 xl2tpd pppd")
os.execute("sleep " .. 1)
os.execute("/tmp/transite-target/bin/xl2tpd --clns "..arg[1].." /tmp/transite-target/etc/options.l2tpd.client")
os.execute("sleep " .. 2)
os.execute("ip route add 10.160.0.0/20 via 10.160.0.1 table 252")
