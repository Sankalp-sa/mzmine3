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

package io.github.mzmine.datamodel.features.compoundannotations;

import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.datamodel.features.ModularFeatureList;
import io.github.mzmine.datamodel.features.ModularFeatureListRow;
import io.github.mzmine.datamodel.features.types.DataType;
import io.github.mzmine.datamodel.features.types.DataTypes;
import io.github.mzmine.datamodel.identities.iontype.IonType;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.io.projectload.version_3_0.CONST;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import io.github.mzmine.parameters.parametertypes.tolerances.RTTolerance;
import io.github.mzmine.parameters.parametertypes.tolerances.mobilitytolerance.MobilityTolerance;
import io.github.mzmine.util.RangeUtils;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Basic class for a compound annotation. The idea is not for it to be observable or so, but to
 * carry a flexible amount of data while providing a set of minimum defined entries.
 */
public class SimpleCompoundDBAnnotation implements
    CompoundDBAnnotation {

  private final Map<DataType<?>, Object> data = new HashMap<>();

  public static final String XML_TYPE_NAME = "simplecompounddbannotation";

  private static final Logger logger = Logger.getLogger(SimpleCompoundDBAnnotation.class.getName());

  @Override
  public <T> T get(@NotNull DataType<T> key) {
    Object value = data.get(key);
    if (value != null && !key.getValueClass().isInstance(value)) {
      throw new IllegalStateException(
          String.format("Value type (%s) does not match data type value class (%s)",
              value.getClass(), key.getValueClass()));
    }
    return (T) value;
  }

  public <T> T get(Class<? extends DataType<T>> key) {
    var actualKey = DataTypes.get(key);
    return get(actualKey);
  }

  @Override
  public <T> T put(@NotNull DataType<T> key, T value) {
    if (value != null && !key.getValueClass().isInstance(value)) {
      throw new IllegalArgumentException(
          String.format("Cannot put value class (%s) for data type (%s). Value type mismatch.",
              value.getClass(), key.getClass()));
    }
    var actualKey = DataTypes.get(key);
    return (T) data.put(actualKey, value);
  }

  public <T> T put(@NotNull Class<? extends DataType<T>> key, T value) {
    var actualKey = DataTypes.get(key);
    if (value != null && !actualKey.getValueClass().isInstance(value)) {
      throw new IllegalArgumentException(
          String.format("Cannot put value class (%s) for data type (%s). Value type mismatch.",
              value.getClass(), actualKey.getClass()));
    }
    return (T) data.put(actualKey, value);
  }

  /**
   * Writes this object to xml using the given writer. A new element for this object is created in
   * the given method.
   *
   * @param writer The writer.
   */
  @Override
  public void saveToXML(@NotNull final XMLStreamWriter writer, ModularFeatureList flist,
      ModularFeatureListRow row) throws XMLStreamException {
    writer.writeStartElement(CompoundDBAnnotation.XML_ELEMENT);
    writer.writeAttribute(CompoundDBAnnotation.XML_TYPE_ATTRIBUTE, XML_TYPE_NAME);
    writer.writeAttribute(CompoundDBAnnotation.XML_NUM_ENTRIES_ATTR, String.valueOf(data.size()));

    for (Entry<DataType<?>, Object> entry : data.entrySet()) {
      final DataType<?> key = entry.getKey();
      final Object value = entry.getValue();

      try {
        writer.writeStartElement(CONST.XML_DATA_TYPE_ELEMENT);
        writer.writeAttribute(CONST.XML_DATA_TYPE_ID_ATTR, key.getUniqueID());
        key.saveToXML(writer, value, flist, row, null, null);
        writer.writeEndElement();
      } catch (XMLStreamException e) {
        final Object finalVal = value;
        logger.warning(
            () -> "Error while writing data type " + key.getClass().getSimpleName() + " with value "
                + finalVal + " to xml.");
        e.printStackTrace();
      }
    }

    writer.writeEndElement();
  }

  public static CompoundDBAnnotation loadFromXML(XMLStreamReader reader, ModularFeatureList flist,
      ModularFeatureListRow row) throws XMLStreamException {
    if (!(reader.isStartElement() && reader.getLocalName().equals(XML_ELEMENT)
        && reader.getAttributeValue(null, XML_TYPE_ATTRIBUTE).equals(XML_TYPE_NAME))) {
      throw new IllegalStateException("Invalid xml element to load CompoundDBAnnotation from.");
    }

    final SimpleCompoundDBAnnotation id = new SimpleCompoundDBAnnotation();
    final int numEntries = Integer.parseInt(reader.getAttributeValue(null, XML_NUM_ENTRIES_ATTR));

    int i = 0;
    while (reader.hasNext() && !(reader.isEndElement() && reader.getLocalName()
        .equals(XML_ELEMENT))) {
      reader.next();

      if (!(reader.isStartElement() && reader.getLocalName().equals(CONST.XML_DATA_TYPE_ELEMENT))) {
        continue;
      }

      final DataType<?> typeForId = DataTypes.getTypeForId(
          reader.getAttributeValue(null, CONST.XML_DATA_TYPE_ID_ATTR));
      if (typeForId != null) {
        Object o = typeForId.loadFromXML(reader, flist, row, null, null);
        id.put((DataType)typeForId, o);
      }
      i++;

      if (i > numEntries) {
        break;
      }
    }

    if (i > numEntries) {
      throw new IllegalStateException(String.format(
          "Finished reading db annotation, but did not find all types. Expected %d, found %d.",
          numEntries, i));
    }
    return id;
  }

  @Override
  public boolean matches(FeatureListRow row, @Nullable MZTolerance mzTolerance,
      @Nullable RTTolerance rtTolerance, @Nullable MobilityTolerance mobilityTolerance,
      @Nullable Double percentCCSTolerance) {

    final Double exactMass = getPrecursorMZ();
    // values are "matched" if the given value exists in this class and falls within the tolerance.
    if (mzTolerance != null && exactMass != null && (row.getAverageMZ() == null
        || !mzTolerance.checkWithinTolerance(row.getAverageMZ(), exactMass))) {
      return false;
    }

    final Float rt = getRT();
    if (rtTolerance != null && rt != null && (row.getAverageRT() == null
        || !rtTolerance.checkWithinTolerance(row.getAverageRT(), rt))) {
      return false;
    }

    final Float mobility = getMobility();
    if (mobilityTolerance != null && mobility != null && (row.getAverageMobility() == null
        || !mobilityTolerance.checkWithinTolerance(mobility, row.getAverageMobility()))) {
      return false;
    }

    final Float ccs = getCCS();
    if (percentCCSTolerance != null && ccs != null && (row.getAverageCCS() == null
        || Math.abs(1 - (row.getAverageCCS() / ccs)) > percentCCSTolerance)) {
      return false;
    }

    return true;
  }

  public Float getScore(FeatureListRow row, @Nullable MZTolerance mzTolerance,
      @Nullable RTTolerance rtTolerance, @Nullable MobilityTolerance mobilityTolerance,
      @Nullable Double percentCCSTolerance) {
    if (!matches(row, mzTolerance, rtTolerance, mobilityTolerance, percentCCSTolerance)) {
      return null;
    }

    int scorers = 0;

    float score = 0f;
    final Double exactMass = getPrecursorMZ();
    // values are "matched" if the given value exists in this class and falls within the tolerance.
    if (mzTolerance != null && exactMass != null && !(row.getAverageMZ() == null
        || !mzTolerance.checkWithinTolerance(row.getAverageMZ(), exactMass))) {
      score += 1 - ((float) ((Math.abs(row.getAverageMZ() - exactMass)) / (
          RangeUtils.rangeLength(mzTolerance.getToleranceRange(exactMass)) / 2)));
      scorers++;
    }

    final Float rt = getRT();
    if (rtTolerance != null && rt != null && !(row.getAverageRT() == null
        || !rtTolerance.checkWithinTolerance(row.getAverageRT(), rt))) {
      score += 1 - ((Math.abs(row.getAverageRT() - rt)) / (
          RangeUtils.rangeLength(rtTolerance.getToleranceRange(rt)) / 2));
      scorers++;
    }

    final Float mobility = getMobility();
    if (mobilityTolerance != null && mobility != null && !(row.getAverageMobility() == null
        || !mobilityTolerance.checkWithinTolerance(mobility, row.getAverageMobility()))) {
      score += 1 - ((Math.abs(row.getAverageMobility() - mobility)) / (
          RangeUtils.rangeLength(mobilityTolerance.getToleranceRange(mobility)) / 2));
      scorers++;
    }

    final Float ccs = getCCS();
    if (percentCCSTolerance != null && ccs != null && !(row.getAverageCCS() == null
        || Math.abs(1 - (row.getAverageCCS() / ccs)) > percentCCSTolerance)) {
      score += 1 - ((float) (Math.abs(1 - (row.getAverageCCS() / ccs)) / percentCCSTolerance));
      scorers++;
    }

    if (scorers == 0) {
      return null;
    }

    return score / scorers;
  }

  @Override
  public CompoundDBAnnotation clone() {
    SimpleCompoundDBAnnotation clone = new SimpleCompoundDBAnnotation();
    data.forEach((key, value) -> clone.put((DataType) key, value));
    return clone;
  }

  @Override
  public String toString() {

    final NumberFormat mzFormat = MZmineCore.getConfiguration().getMZFormat();
    final NumberFormat scoreFormat = MZmineCore.getConfiguration().getScoreFormat();
    final IonType adductType = getAdductType();

    final StringBuilder b = new StringBuilder();

    if(getCompundName()!= null) {
      b.append(getCompundName()).append(",");
    }
    if(getAdductType() != null) {
      b.append(" ").append(getAdductType().toString(false)).append(", ");
    }
    if(getPrecursorMZ() != null) {
      b.append(mzFormat.format(getPrecursorMZ())).append(", ");
    }
    if(getScore() != null) {
      b.append(scoreFormat.format(getScore()));
    }
    return b.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SimpleCompoundDBAnnotation)) {
      return false;
    }
    SimpleCompoundDBAnnotation that = (SimpleCompoundDBAnnotation) o;
    return data.equals(that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }
}

