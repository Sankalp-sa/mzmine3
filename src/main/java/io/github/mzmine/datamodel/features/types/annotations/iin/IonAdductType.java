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

package io.github.mzmine.datamodel.features.types.annotations.iin;

import io.github.mzmine.datamodel.features.types.abstr.StringType;
import io.github.mzmine.datamodel.features.types.modifiers.AnnotationType;
import io.github.mzmine.datamodel.features.types.modifiers.EditableColumnType;
import io.github.mzmine.datamodel.features.types.modifiers.StringParser;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.jetbrains.annotations.NotNull;

/**
 * The ion adduct is used in different {@link AnnotationType}s to represent the ion species. Is
 * going to be replaced by the {@link IonTypeType}
 */
@Deprecated
public class IonAdductType extends StringType
    implements EditableColumnType, StringParser<String>, AnnotationType {

  private StringConverter<String> converter = new DefaultStringConverter();

  @Override
  public @NotNull String getHeaderString() {
    return "Adduct";
  }

  @Override
  public String fromString(String s) {
    return s;
  }

  @Override
  public StringConverter<String> getStringConverter() {
    return converter;
  }

  @NotNull
  @Override
  public final String getUniqueID() {
    // Never change the ID for compatibility during saving/loading of type
    return "ion_adduct";
  }

}
