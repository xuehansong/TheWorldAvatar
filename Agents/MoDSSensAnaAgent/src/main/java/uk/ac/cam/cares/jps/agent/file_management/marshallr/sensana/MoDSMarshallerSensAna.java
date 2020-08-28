package uk.ac.cam.cares.jps.agent.file_management.marshallr.sensana;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.agent.file_management.marshallr.ExecutableModel;
import uk.ac.cam.cares.jps.agent.file_management.marshallr.MoDSMarshaller;
import uk.ac.cam.cares.jps.agent.mechanism.sensana.MoDSSensAnaAgentException;

public class MoDSMarshallerSensAna extends MoDSMarshaller {
	private static Logger logger = LoggerFactory.getLogger(MoDSMarshallerSensAna.class);
	
	@Override
	public void plugInKinetics(List<String> experimentIRI, String mechanismIRI, List<String> reactionIRIList, String otherOptions) throws IOException, MoDSSensAnaAgentException {
		// TODO Auto-generated method stub
		ModelKineticsSRMSensAna kineticsSRM = new ModelKineticsSRMSensAna();
		ExecutableModel exeModel = kineticsSRM.formExecutableModel(experimentIRI, mechanismIRI, reactionIRIList);
		kineticsSRM.formFiles(exeModel, otherOptions);
		kineticsSRM.setUpMoDS();
		kineticsSRM.placeScript();
		
		logger.info("Model kineticsSRM was added to the MoDS job.");
	}
	
	@Override
	public void plugInCantera(List<String> experimentIRI, String mechanismIRI, List<String> reactionIRIList, String otherOptions) throws IOException, MoDSSensAnaAgentException {
		// TODO Auto-generated method stub
		ModelCanteraLFSSensAna canteraLFS = new ModelCanteraLFSSensAna();
		ExecutableModel exeModel = canteraLFS.formExecutableModel(experimentIRI, mechanismIRI, reactionIRIList);
		canteraLFS.formFiles(exeModel, otherOptions);
		canteraLFS.setUpMoDS();
		canteraLFS.placeScript();
		
		logger.info("Model canteraLFS was added to the MoDS job.");
	}
}