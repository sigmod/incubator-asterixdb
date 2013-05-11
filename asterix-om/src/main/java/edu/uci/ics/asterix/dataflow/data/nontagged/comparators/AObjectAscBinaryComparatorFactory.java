package edu.uci.ics.asterix.dataflow.data.nontagged.comparators;

import edu.uci.ics.asterix.om.types.ATypeTag;
import edu.uci.ics.asterix.om.types.EnumDeserializer;
import edu.uci.ics.hyracks.algebricks.common.exceptions.NotImplementedException;
import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparator;
import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparatorFactory;
import edu.uci.ics.hyracks.data.std.accessors.PointableBinaryComparatorFactory;
import edu.uci.ics.hyracks.data.std.primitive.DoublePointable;
import edu.uci.ics.hyracks.data.std.primitive.FloatPointable;
import edu.uci.ics.hyracks.data.std.primitive.IntegerPointable;
import edu.uci.ics.hyracks.data.std.primitive.UTF8StringPointable;

public class AObjectAscBinaryComparatorFactory implements IBinaryComparatorFactory {

    private static final long serialVersionUID = 1L;

    public static final AObjectAscBinaryComparatorFactory INSTANCE = new AObjectAscBinaryComparatorFactory();

    private AObjectAscBinaryComparatorFactory() {
    }

    @Override
    public IBinaryComparator createBinaryComparator() {
        return new IBinaryComparator() {
            final IBinaryComparator ascBoolComp = BooleanBinaryComparatorFactory.INSTANCE.createBinaryComparator();
            final IBinaryComparator ascIntComp = new PointableBinaryComparatorFactory(IntegerPointable.FACTORY)
                    .createBinaryComparator();
            final IBinaryComparator ascLongComp = LongBinaryComparatorFactory.INSTANCE.createBinaryComparator();
            final IBinaryComparator ascStrComp = new PointableBinaryComparatorFactory(UTF8StringPointable.FACTORY)
                    .createBinaryComparator();
            final IBinaryComparator ascFloatComp = new PointableBinaryComparatorFactory(FloatPointable.FACTORY)
                    .createBinaryComparator();
            final IBinaryComparator ascDoubleComp = new PointableBinaryComparatorFactory(DoublePointable.FACTORY)
                    .createBinaryComparator();
            final IBinaryComparator ascRectangleComp = RectangleBinaryComparatorFactory.INSTANCE
                    .createBinaryComparator();
            final IBinaryComparator ascCircleComp = ACirclePartialBinaryComparatorFactory.INSTANCE
                    .createBinaryComparator();
            final IBinaryComparator ascDurationComp = ADurationPartialBinaryComparatorFactory.INSTANCE
                    .createBinaryComparator();
            final IBinaryComparator ascIntervalComp = AIntervalPartialBinaryComparatorFactory.INSTANCE
                    .createBinaryComparator();
            final IBinaryComparator ascLineComp = ALinePartialBinaryComparatorFactory.INSTANCE.createBinaryComparator();
            final IBinaryComparator ascPointComp = APointPartialBinaryComparatorFactory.INSTANCE
                    .createBinaryComparator();
            final IBinaryComparator ascPoint3DComp = APoint3DPartialBinaryComparatorFactory.INSTANCE
                    .createBinaryComparator();
            final IBinaryComparator ascPolygonComp = APolygonPartialBinaryComparatorFactory.INSTANCE
                    .createBinaryComparator();

            @Override
            public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {

                if (b1[s1] == ATypeTag.NULL.serialize()) {
                    if (b2[s2] == ATypeTag.NULL.serialize())
                        return 0;
                    else
                        return -1;
                } else {
                    if (b2[s2] == ATypeTag.NULL.serialize())
                        return 1;
                }

                ATypeTag tag = EnumDeserializer.ATYPETAGDESERIALIZER.deserialize(b1[s1]);
                switch (tag) {
                    case BOOLEAN: {
                        return ascBoolComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case TIME:
                    case DATE:
                    case YEARMONTHDURATION:
                    case INT32: {
                        return ascIntComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case DATETIME:
                    case DAYTIMEDURATION:
                    case INT64: {
                        return ascLongComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case FLOAT: {
                        return ascFloatComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case DOUBLE: {
                        return ascDoubleComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case STRING: {
                        return ascStrComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case RECTANGLE: {
                        return ascRectangleComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case CIRCLE: {
                        return ascCircleComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case POINT: {
                        return ascPointComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case POINT3D: {
                        return ascPoint3DComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case LINE: {
                        return ascLineComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case POLYGON: {
                        return ascPolygonComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case DURATION: {
                        return ascDurationComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    case INTERVAL: {
                        return ascIntervalComp.compare(b1, s1 + 1, l1 - 1, b2, s2 + 1, l2 - 1);
                    }
                    default: {
                        throw new NotImplementedException("Comparison for type " + tag + " is not implemented");
                    }
                }
            }
        };
    }
}
