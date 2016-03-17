/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.LinearAttenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.LogScanner;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryLogMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryLogMetric;

/**
 * @author Administrator
 *TODO
 */
public class Test {

	@org.junit.Test
	public void test() throws Exception {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.showOpenDialog(null);
		File selectedFile = chooser.getSelectedFile();
		List<XLog> logs = null;
		for(XParser parser : XParserRegistry.instance().getAvailable()){
			if(parser.canParse(selectedFile)){
				logs = parser.parse(selectedFile);
			}
		}
		
		for(XLog  log :logs){
			MetricsRepository mr = MetricsRepository.createRepository(XLogInfoFactory.createLogInfo(log));
			Attenuation att = new LinearAttenuation(100, 10);
			mr.apply(log, att, 1);
			List<BinaryLogMetric> metrics = mr.getBinaryLogMetrics();
			for(BinaryLogMetric metric : metrics){
				int size = XLogInfoFactory.createLogInfo(log).getNumberOfEvents();
			
				for(int x= 0; x < size; x++){
					for(int y = 0; y < size; y++){
						System.out.print(metric.getMeasure(x, y) + " ");;
					}
					System.out.println();
				}
			}
		}
		//XLog log;
		
		//int maxLookBack = 100;
		//LogScanner scanner = new LogScanner();
//		for(XLog log : logs){
//			
//			MetricsRepository mr = MetricsRepository.createRepository(XLogInfoFactory.createLogInfo(log));
//			Attenuation att = new LinearAttenuation(100, 10);
//			mr.apply(log, att , 1);
//			List<UnaryLogMetric> unaryLogMetrics = mr.getUnaryLogMetrics();
//			for(UnaryLogMetric metric : unaryLogMetrics){
//				for(int i = 0; i < metric.size(); i++){
//					System.out.println(metric.getNormalizedValues()[i]+" ");
//				}
//			}
//		}
	}

}
