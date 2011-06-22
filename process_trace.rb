#!/usr/bin/env ruby
#
# Reads a call trace, formatted as
#
# [<>][stack_depth][thread]called_method=time_nanosecs
#
# and produces the total time spent in each method.
# Lines that start with > denote method entry
#                       < denote method exit
# (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
#

require 'rubygems'

if ARGV.size <= 0 then
   print "usage: timings.rb timings file" 
end

results = Hash.new
mstack = Array.new
tstack = Array.new
total_time_at_depth = Hash.new
lines_read = 0
d = 0

File.open(ARGV[0]).each do |line|
    type, depth, tid, method, time = line.scan(/([<>])\[(\d+)\]\[(\d+)\](.*)=(\d+)/)[0]
    lines_read += 1
    depth = depth.to_i
    time = time.to_i

    case type
    when '<':
        # Can only return from inner or same depth level frame
        if not [d, d - 1].include? depth then
            print "Return from stack depth: ", d,
                  " to depth:", depth, ", line ",
                  lines_read, "\n"
            return
        end
        m = mstack.pop
        t = tstack.pop

        # Sanity check
        if not m == method then
            print "Method ", method, " not found at depth ",
                  depth, " line ", lines_read, "\n"
            return
        end

        # Total time for a method is the time to execute it minus the time to
        # invoke all called methods
        method_time = time - t
        results[method] = results.fetch(method, 0) + method_time -
                          total_time_at_depth.fetch(depth + 1, 0)

        # Sanity check
        if method_time - total_time_at_depth.fetch(depth + 1, 0) < 0 then
            print "Time required for method #{method} (#{method_time}) less
                   than time for required for inner frame
                   (#{total_time_at_depth.fetch(depth + 1, 0)})! ",
                   "Line ", lines_read, "\n"
            return
        end

        # Total for a frame is the total time of its first level decendant
        # plus the time required to execute all methods at the same depth
        total_time_at_depth[depth] = method_time -
                                     total_time_at_depth.fetch(depth + 1, 0) +
                                     total_time_at_depth.fetch(depth, 0)
        # Inner stack frame time was used already at this depth,
        # delete to avoid reusing
        total_time_at_depth.delete(depth + 1)

        # Reset total time for depth 0 after it has been used
        # since the statement above never runs at this depth
        if depth == 0:
          total_time_at_depth.delete(depth)
        end
    when '>':
        # Can only go into an inner or same depth level frame
        if not [d, d + 1].include? depth then
            print "Jump from stack depth: #{d} to depth: #{depth},
                   line #{lines_read} \n"
            return
        end
        mstack.push method
        tstack.push time
    else print "Cannot parse line #{line}"
    end
    d = depth
end

# Print results sorted
results.sort{|a,b| b[1]<=>a[1]}.each {|x|
  print "#{x[0]} #{x[1]}\n"
}
