/*
 * Copyright 2006-2012 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.masslistmethods.chromatogrambuilder;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.MZToleranceParameter;
import net.sf.mzmine.parameters.parametertypes.MassListParameter;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.RawDataFilesParameter;
import net.sf.mzmine.parameters.parametertypes.StringParameter;

public class ChromatogramBuilderParameters extends SimpleParameterSet {

	public static final RawDataFilesParameter dataFiles = new RawDataFilesParameter();

	public static final MassListParameter massList = new MassListParameter();

	public static final DoubleParameter minimumTimeSpan = new DoubleParameter(
			"Min time span",
			"Minimum time span over which the same ion must be observed in order to be recognized as a chromatogram. "
					+ "The optimal value depends on the chromatography system setup. The best way to set this parameter "
					+ "is by studying the raw data and determining what is the typical time span of chromatographic peaks.",
			MZmineCore.getRTFormat());

	public static final DoubleParameter minimumHeight = new DoubleParameter(
			"Min height",
			"Minimum intensity of the highest data point in the chromatogram. If chromatogram height is below this level, it is discarded.",
			MZmineCore.getIntensityFormat());

	public static final MZToleranceParameter mzTolerance = new MZToleranceParameter(
			"m/z tolerance",
			"Maximum allowed distance in M/Z between data points in successive spectrums");

	public static final StringParameter suffix = new StringParameter("Suffix",
			"This string is added to filename as suffix", "chromatograms");

	public ChromatogramBuilderParameters() {
		super(new Parameter[] { dataFiles, massList, minimumTimeSpan,
				minimumHeight, mzTolerance, suffix });
	}

}
