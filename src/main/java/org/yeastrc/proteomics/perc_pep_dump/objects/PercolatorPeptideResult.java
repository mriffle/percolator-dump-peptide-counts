package org.yeastrc.proteomics.perc_pep_dump.objects;

import java.util.Collection;

public class PercolatorPeptideResult {

	private Collection<PercolatorPSM> psmCollection;
	private PercolatorPeptideStats percolatorPeptideStats;
	private String reportedPeptide;

	/**
	 * @return the percolatorPeptideStats
	 */
	public PercolatorPeptideStats getPercolatorPeptideStats() {
		return percolatorPeptideStats;
	}
	/**
	 * @param percolatorPeptideStats the percolatorPeptideStats to set
	 */
	public void setPercolatorPeptideStats(PercolatorPeptideStats percolatorPeptideStats) {
		this.percolatorPeptideStats = percolatorPeptideStats;
	}
	/**
	 * @return the reportedPeptide
	 */
	public String getReportedPeptide() {
		return reportedPeptide;
	}
	/**
	 * @param reportedPeptide the reportedPeptide to set
	 */
	public void setReportedPeptide(String reportedPeptide) {
		this.reportedPeptide = reportedPeptide;
	}

	public Collection<PercolatorPSM> getPsmCollection() {
		return psmCollection;
	}

	public void setPsmCollection(Collection<PercolatorPSM> psmCollection) {
		this.psmCollection = psmCollection;
	}
}
