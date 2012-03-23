package edu.uci.ics.asterix.runtime.evaluators.functions;

import edu.uci.ics.asterix.common.functions.FunctionConstants;
import edu.uci.ics.asterix.om.types.ARecordType;
import edu.uci.ics.asterix.runtime.evaluators.base.AbstractScalarFunctionDynamicDescriptor;
import edu.uci.ics.asterix.runtime.evaluators.common.ClosedRecordConstructorEvalFactory;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import edu.uci.ics.hyracks.algebricks.core.algebra.runtime.base.IEvaluatorFactory;

public class ClosedRecordConstructorDescriptor extends AbstractScalarFunctionDynamicDescriptor {

    protected static final FunctionIdentifier FID_CLOSED = new FunctionIdentifier(FunctionConstants.ASTERIX_NS,
            "closed-record-constructor", FunctionIdentifier.VARARGS, true);

    private static final long serialVersionUID = 1L;

    private ARecordType recType;

    public void reset(ARecordType recType) {
        this.recType = recType;
    }

    @Override
    public FunctionIdentifier getIdentifier() {
        return FID_CLOSED;
    }

    @Override
    public IEvaluatorFactory createEvaluatorFactory(final IEvaluatorFactory[] args) {
        return new ClosedRecordConstructorEvalFactory(args, recType);
    }

}