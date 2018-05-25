#!/tmp/transite-target/bin/lua
cmd = ""
for i, v in pairs(arg) do
  if(i>0)
  then
    cmd=cmd.." "..v
  end
end
os.execute(cmd)
