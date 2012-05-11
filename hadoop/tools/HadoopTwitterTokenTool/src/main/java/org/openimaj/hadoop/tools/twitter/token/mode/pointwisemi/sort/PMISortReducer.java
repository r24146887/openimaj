package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.TokenPairUnaryCount;
import org.openimaj.io.IOUtils;
import org.openimaj.util.pair.IndependentPair;

import cern.colt.Arrays;

import com.Ostermiller.util.CSVPrinter;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PMISortReducer extends Reducer<Text, BytesWritable, NullWritable,Text> {
	/**
	 * 
	 */
	public PMISortReducer(){
		
	}
	@Override
	protected void reduce(Text timepmi, Iterable<BytesWritable> textvalues, Reducer<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException ,InterruptedException {
		String[] firstsecond = new String[2];
		for (BytesWritable value : textvalues) {
			IndependentPair<Long, Double> timepmii = PMIPairSort.parseTimeStr(timepmi.toString());
			long time = timepmii.firstObject();
			TokenPairUnaryCount tpuc = IOUtils.deserialize(value.getBytes(), TokenPairUnaryCount.class);
			StringWriter swrit = new StringWriter();
			CSVPrinter csvp = new CSVPrinter(swrit);
			firstsecond[0] = tpuc.firstObject();
			firstsecond[1] = tpuc.secondObject();
			csvp.write(new String[]{time+"",Arrays.toString(firstsecond),tpuc.paircount+"",tpuc.tok1count+"",tpuc.tok2count+"",timepmii.secondObject()+""});
			csvp.flush();
			context.write(NullWritable.get(), new Text(swrit.toString()));
		}
	};
}