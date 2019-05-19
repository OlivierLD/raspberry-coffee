#
# Interactive (REPL) Ruby: $ irb
#
words = ["one", "two", "three"]
puts words[1]

idx = 0
limit = 10
while idx < limit do
  print idx, ". Ruby loop\n"
  idx += 1
end

for x in 0..5
  puts "Value of x is #{x}"
end

for x in 0..5
  if x > 2 then
    break
  end
  puts "Value of x is #{x}"
end
