/*
 * Copyright 2006-2021 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package io.github.mzmine.modules.visualization.kendrickmassplot;

import java.text.DecimalFormat;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.ComboParameter;
import io.github.mzmine.parameters.parametertypes.OptionalParameter;
import io.github.mzmine.parameters.parametertypes.StringParameter;
import io.github.mzmine.parameters.parametertypes.WindowSettingsParameter;
import io.github.mzmine.parameters.parametertypes.ranges.DoubleRangeParameter;
import io.github.mzmine.parameters.parametertypes.selectors.FeatureListsParameter;
import io.github.mzmine.parameters.parametertypes.selectors.FeatureSelectionParameter;
import io.github.mzmine.util.ExitCode;

/**
 * parameters for Kendrick mass plots
 *
 * @author Ansgar Korf (ansgar.korf@uni-muenster.de)
 */
public class KendrickMassPlotParameters extends SimpleParameterSet {
  public static final FeatureListsParameter featureList = new FeatureListsParameter(1, 1);

  public static final FeatureSelectionParameter selectedRows = new FeatureSelectionParameter();

  public static final StringParameter yAxisCustomKendrickMassBase =
      new StringParameter("Kendrick mass base for y-Axis",
          "Enter a sum formula for a Kendrick mass base, e.g. \"CH2\" ");

  public static final ComboParameter<String> xAxisValues = new ComboParameter<>("X-Axis",
      "Select Kendrick mass (KM) or m/z", new String[] {"m/z", "KM"});

  public static final OptionalParameter<StringParameter> xAxisCustomKendrickMassBase =
      new OptionalParameter<>(new StringParameter("Kendrick mass base for x-Axis",
          "Enter a sum formula for a Kendrick mass base to display a 2D Kendrick mass defect plot"));

  public static final ComboParameter<String> zAxisValues = new ComboParameter<>("Z-Axis",
      "Select a parameter for a third dimension, displayed as a heatmap or select none for a 2D plot",
      new String[] {"none", "Retention time", "Intensity", "Area", "Tailing factor",
          "Asymmetry factor", "FWHM", "m/z"});

  public static final OptionalParameter<StringParameter> zAxisCustomKendrickMassBase =
      new OptionalParameter<>(new StringParameter("Kendrick mass base for z-Axis",
          "Enter a sum formula for a Kendrick mass base to display a Kendrick mass defect in form of a heatmap"));

  public static final ComboParameter<String> bubbleSize = new ComboParameter<>("Bubble Size",
      "Select a parameter for a third dimension, displayed as a heatmap or select none for a 2D plot",
      new String[] {"none", "Retention time", "Intensity", "Area", "Tailing factor",
          "Asymmetry factor", "FWHM", "m/z"});

  public static final ComboParameter<String> zScaleType = new ComboParameter<>("Z-Axis scale",
      "Select Z-Axis scale", new String[] {"percentile", "custom"});

  public static final DoubleRangeParameter zScaleRange = new DoubleRangeParameter(
      "Range for z-Axis scale",
      "Set the range for z-Axis scale."
          + " If percentile is used for z-Axis scale type, you can remove extreme values of the scale."
          + " E. g. type 0.5 and 99.5 to ignore the 0.5 smallest and 0.5 highest values. "
          + "If you choose custom, set ranges manually "
          + "Features out of scale range are displayed in magenta",
      new DecimalFormat("##0.00"));

  public static final ComboParameter<String> paintScale = new ComboParameter<>("Heatmap style",
      "Select the style for the third dimension", new String[] {"Rainbow", "Monochrome red",
          "Monochrome green", "Monochrome yellow", "Monochrome cyan"});

  public static final WindowSettingsParameter windowSettings = new WindowSettingsParameter();

  public KendrickMassPlotParameters() {
    super(new Parameter[] {featureList, selectedRows, yAxisCustomKendrickMassBase, xAxisValues,
        xAxisCustomKendrickMassBase, zAxisValues, zAxisCustomKendrickMassBase, bubbleSize,
        zScaleType, zScaleRange, paintScale, windowSettings},
        "https://mzmine.github.io/mzmine_documentation/visualization_modules/processed_additional/processed_additional.md#kendrick-mass-plot");
  }

  @Override
  public ExitCode showSetupDialog(boolean valueCheckRequired) {
    return super.showSetupDialog(valueCheckRequired);
  }

}
