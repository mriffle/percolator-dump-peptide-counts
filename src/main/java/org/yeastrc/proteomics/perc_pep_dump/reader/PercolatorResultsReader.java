/*
 * Original author: Michael Riffle <mriffle .at. uw.edu>
 *
 * Copyright 2018 University of Washington - Seattle, WA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yeastrc.proteomics.perc_pep_dump.reader;

import org.yeastrc.proteomics.perc_pep_dump.objects.*;

import org.yeastrc.proteomics.percolator.out.PercolatorOutXMLUtils;
import org.yeastrc.proteomics.percolator.out.perc_out_common_interfaces.IPeptide;
import org.yeastrc.proteomics.percolator.out.perc_out_common_interfaces.IPercolatorOutput;
import org.yeastrc.proteomics.percolator.out.perc_out_common_interfaces.IPsm;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PercolatorResultsReader {

	/**
	 * Get the parsed percolator results for the given percolator xml data file
	 *
	 * @param file
	 * @return
	 * @throws Throwable
	 */
	public static PercolatorResults getPercolatorResults( File file ) throws Throwable {

		IPercolatorOutput po = getIPercolatorOutput( file );

		Map<String, PercolatorPSM> psmIdPSMMap = getPercolatorPSMs(po);
		Map<ReportedPeptide, PercolatorPeptideResult> peptideResults = getPercolatorPeptidePSMMap( po, psmIdPSMMap );

		psmIdPSMMap = null;
		po = null;

		PercolatorResults results = new PercolatorResults();
		results.setPeptideResults( peptideResults );

		return results;
	}


	/**
	 * Get a map of percolator peptide => map of scan number => percolator psm
	 *
	 * @param po The IPercolatorOutput JAXB object created from parsing the XML
	 * @param psmIdPSMMap A map of all PercolatorPSMs found, keyed on their reported psm id string
	 * @return
	 * @throws Exception
	 */
	protected static Map<ReportedPeptide, PercolatorPeptideResult> getPercolatorPeptidePSMMap(IPercolatorOutput po, Map<String, PercolatorPSM> psmIdPSMMap ) throws Exception {

		Map<ReportedPeptide, PercolatorPeptideResult> resultsMap = new HashMap<>();

		// loop through the reported peptides
	    for( IPeptide xpeptide : po.getPeptides().getPeptide() ) {

	    	PercolatorPeptideStats percolatorPeptide = getPercolatorPeptideFromJAXB( xpeptide );

	    	if(resultsMap.containsKey( new ReportedPeptide(percolatorPeptide.getReportedPeptide())))
	    		throw new Exception( "Found two instances of the same reported peptide: " + percolatorPeptide + " and " + resultsMap.get( percolatorPeptide ) );

	    	Collection<PercolatorPSM> psmsForPeptide = getPercolatorPSMsForPeptide( xpeptide, psmIdPSMMap );

	    	if( psmsForPeptide == null || psmsForPeptide.size() < 1 )
	    		throw new Exception( "Found no PSMs for peptide: " + percolatorPeptide );

	    	PercolatorPeptideResult result = new PercolatorPeptideResult();
	    	result.setPercolatorPeptideStats( percolatorPeptide );
	    	result.setPsmCollection( psmsForPeptide );
	    	result.setReportedPeptide( percolatorPeptide.getReportedPeptide() );

	    	resultsMap.put( new ReportedPeptide(result.getReportedPeptide()), result );
	    }


		return resultsMap;
	}


	/**
	 * Get a Collection of all PSMs associated with the supplied JAXB peptide object
	 * @param xpeptide
	 * @param psmIdPSMMap
	 * @return
	 * @throws Exception
	 */
	protected static Collection<PercolatorPSM> getPercolatorPSMsForPeptide(IPeptide xpeptide, Map<String, PercolatorPSM> psmIdPSMMap ) throws Exception {

		Collection<PercolatorPSM> psmsForPeptide = new HashSet<>();

		for( String psmId : xpeptide.getPsmIds().getPsmId() ) {

			if( !psmIdPSMMap.containsKey( psmId ) )
				throw new Exception( "Peptide contains psmId: " + psmId + ", but no PSM with that id was found. Peptide: " + xpeptide.getPeptideId() );

			PercolatorPSM psm = psmIdPSMMap.get( psmId );

			if( !psm.getReportedPeptide().equals( xpeptide.getPeptideId() ) )
				throw new Exception( "PSM (" + psm + ") has a different reported peptide than this peptide id: " + xpeptide.getPeptideId() );

			psmsForPeptide.add(psm);
		}

		return psmsForPeptide;
	}



	/**
	 * Get the PercolatorPeptide object for the given JAXB representation of a percolator peptide
	 *
	 * @param xpeptide
	 * @return
	 */
	protected static PercolatorPeptideStats getPercolatorPeptideFromJAXB(IPeptide xpeptide ) {

		PercolatorPeptideStats pp = new PercolatorPeptideStats();

		pp.setPep( Double.valueOf( xpeptide.getPep() ) );
		pp.setpValue( Double.valueOf( xpeptide.getPValue() ) );
		pp.setqValue( Double.valueOf( xpeptide.getQValue() ) );
		pp.setReportedPeptide( xpeptide.getPeptideId() );
		pp.setSvmScore( Double.valueOf( xpeptide.getSvmScore() ) );

		return pp;
	}


	/**
	 * Return a collection of all the PercolatorPSMs parsed from the JAXB top level percolator XML object
	 *
	 * @param po
	 * @return
	 */
	protected static Map<String, PercolatorPSM> getPercolatorPSMs(IPercolatorOutput po) {

		Map<String, PercolatorPSM> psmIdPSMMap = new HashMap<>();

	    // loop through PSMs
	    for( IPsm xpsm : po.getPsms().getPsm() ) {

	    	PercolatorPSM psm = getPercolatorPSMFromJAXB( xpsm );
	    	psmIdPSMMap.put( psm.getPsmId(), psm );

	    }

		return psmIdPSMMap;
	}

	/**
	 * Get a PercolatorPSM from the JAXB object generated from parsing the XML
	 * @param xpsm
	 * @return
	 */
	protected static PercolatorPSM getPercolatorPSMFromJAXB( IPsm xpsm ) {

		PercolatorPSM psm = new PercolatorPSM();

		psm.setPep( Double.valueOf( xpsm.getPep() ) );
		psm.setPsmId( xpsm.getPsmId() );
		psm.setpValue( Double.valueOf( xpsm.getPValue() ) );
		psm.setqValue( Double.valueOf( xpsm.getQValue() ) );
		psm.setReportedPeptide( xpsm.getPeptideSeq().getSeq() );
		psm.setSvmScore( Double.valueOf( xpsm.getSvmScore() ) );

		return psm;
	}

	/**
	 * Get the top level JAXB object for the given percolator XML file
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	protected static IPercolatorOutput getIPercolatorOutput( File file ) throws Exception {

		String xsdVersion = PercolatorOutXMLUtils.getXSDVersion( file );

		JAXBContext jaxbContext = JAXBContext.newInstance( "com.per_colator.percolator_out._" + xsdVersion );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		IPercolatorOutput po = (IPercolatorOutput)u.unmarshal( file );

		return po;
	}


}
