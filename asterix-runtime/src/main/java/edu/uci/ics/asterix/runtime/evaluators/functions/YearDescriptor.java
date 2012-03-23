package edu.uci.ics.asterix.runtime.evaluators.functions;

import java.io.DataOutput;

import edu.uci.ics.asterix.common.functions.FunctionConstants;
import edu.uci.ics.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import edu.uci.ics.asterix.om.base.AInt32;
import edu.uci.ics.asterix.om.base.AMutableInt32;
import edu.uci.ics.asterix.om.base.ANull;
import edu.uci.ics.asterix.om.types.ATypeTag;
import edu.uci.ics.asterix.om.types.BuiltinType;
import edu.uci.ics.asterix.om.types.EnumDeserializer;
import edu.uci.ics.asterix.runtime.evaluators.base.AbstractScalarFunctionDynamicDescriptor;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import edu.uci.ics.hyracks.algebricks.core.algebra.runtime.base.IEvaluator;
import edu.uci.ics.hyracks.algebricks.core.algebra.runtime.base.IEvaluatorFactory;
import edu.uci.ics.hyracks.algebricks.core.api.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.api.dataflow.value.ISerializerDeserializer;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.data.std.primitive.UTF8StringPointable;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.ArrayBackedValueStorage;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.IDataOutputProvider;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.IFrameTupleReference;
import edu.uci.ics.hyracks.dataflow.common.data.util.StringUtils;

public class YearDescriptor extends AbstractScalarFunctionDynamicDescriptor {

    private static final long serialVersionUID = 1L;
    public final static FunctionIdentifier FID = new FunctionIdentifier(FunctionConstants.ASTERIX_NS, "year", 1, true);
    private final static byte SER_NULL_TYPE_TAG = ATypeTag.NULL.serialize();
    private final static byte SER_STRING_TYPE_TAG = ATypeTag.STRING.serialize();

    @Override
    public FunctionIdentifier getIdentifier() {
        return FID;
    }

    /**
     * Returns the 4-digit representation of a year from a string, as an int32.
     * e.g. year('2010-10-24') = 2010
     */

    @Override
    public IEvaluatorFactory createEvaluatorFactory(final IEvaluatorFactory[] args) {

        return new IEvaluatorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public IEvaluator createEvaluator(IDataOutputProvider output) throws AlgebricksException {
                final DataOutput out = output.getDataOutput();

                return new IEvaluator() {
                    private ArrayBackedValueStorage out1 = new ArrayBackedValueStorage();
                    private IEvaluator eval1 = args[0].createEvaluator(out1);
                    private AMutableInt32 m = new AMutableInt32(0);
                    @SuppressWarnings("unchecked")
                    private ISerializerDeserializer<AInt32> int32Serde = AqlSerializerDeserializerProvider.INSTANCE
                            .getSerializerDeserializer(BuiltinType.AINT32);
                    @SuppressWarnings("unchecked")
                    private ISerializerDeserializer<ANull> nullSerde = AqlSerializerDeserializerProvider.INSTANCE
                            .getSerializerDeserializer(BuiltinType.ANULL);

                    @Override
                    public void evaluate(IFrameTupleReference tuple) throws AlgebricksException {
                        try {
                            out1.reset();
                            eval1.evaluate(tuple);
                            byte[] dateArray = out1.getBytes();

                            if (dateArray[0] == SER_NULL_TYPE_TAG) {
                                nullSerde.serialize(ANull.NULL, out);
                                return;
                            }

                            if (dateArray[0] != SER_STRING_TYPE_TAG) {
                                throw new AlgebricksException("year function can not be called on values of type"
                                        + EnumDeserializer.ATYPETAGDESERIALIZER.deserialize(dateArray[0]));
                            }

                            int year = (UTF8StringPointable.charAt(dateArray, 3) - '0') * 1000
                                    + (UTF8StringPointable.charAt(dateArray, 4) - '0') * 100
                                    + (UTF8StringPointable.charAt(dateArray, 5) - '0') * 10
                                    + (UTF8StringPointable.charAt(dateArray, 6) - '0');
                            m.setValue(year);

                            int32Serde.serialize(m, out);
                        } catch (HyracksDataException e) {
                            throw new AlgebricksException(e);
                        }
                    }
                };
            }
        };
    }

}