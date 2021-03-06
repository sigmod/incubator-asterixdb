/*
 * Copyright 2009-2013 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.asterix.metadata.entitytupletranslators;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import edu.uci.ics.asterix.common.config.DatasetConfig.ExternalFilePendingOp;
import edu.uci.ics.asterix.common.exceptions.AsterixException;
import edu.uci.ics.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import edu.uci.ics.asterix.metadata.MetadataException;
import edu.uci.ics.asterix.metadata.bootstrap.MetadataPrimaryIndexes;
import edu.uci.ics.asterix.metadata.bootstrap.MetadataRecordTypes;
import edu.uci.ics.asterix.metadata.entities.ExternalFile;
import edu.uci.ics.asterix.om.base.ADateTime;
import edu.uci.ics.asterix.om.base.AInt32;
import edu.uci.ics.asterix.om.base.AInt64;
import edu.uci.ics.asterix.om.base.AMutableDateTime;
import edu.uci.ics.asterix.om.base.AMutableInt32;
import edu.uci.ics.asterix.om.base.AMutableInt64;
import edu.uci.ics.asterix.om.base.ARecord;
import edu.uci.ics.asterix.om.base.AString;
import edu.uci.ics.asterix.om.types.BuiltinType;
import edu.uci.ics.hyracks.api.dataflow.value.ISerializerDeserializer;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.ITupleReference;

public class ExternalFileTupleTranslator extends AbstractTupleTranslator<ExternalFile> {
    // Field indexes of serialized ExternalFile in a tuple.
    // First key field.
    public static final int EXTERNAL_FILE_DATAVERSENAME_TUPLE_FIELD_INDEX = 0;
    // Second key field.
    public static final int EXTERNAL_FILE_DATASETNAME_TUPLE_FIELD_INDEX = 1;
    // Third key field
    public static final int EXTERNAL_FILE_NUMBER_TUPLE_FIELD_INDEX = 2;
    // Payload field containing serialized ExternalFile.
    public static final int EXTERNAL_FILE_PAYLOAD_TUPLE_FIELD_INDEX = 3;

    protected AMutableInt32 aInt32 = new AMutableInt32(0);
    protected AMutableDateTime aDateTime = new AMutableDateTime(0);
    protected AMutableInt64 aInt64 = new AMutableInt64(0);

    @SuppressWarnings("unchecked")
    protected ISerializerDeserializer<AInt32> intSerde = AqlSerializerDeserializerProvider.INSTANCE
            .getSerializerDeserializer(BuiltinType.AINT32);
    @SuppressWarnings("unchecked")
    protected ISerializerDeserializer<ADateTime> dateTimeSerde = AqlSerializerDeserializerProvider.INSTANCE
            .getSerializerDeserializer(BuiltinType.ADATETIME);
    @SuppressWarnings("unchecked")
    protected ISerializerDeserializer<AInt64> longSerde = AqlSerializerDeserializerProvider.INSTANCE
            .getSerializerDeserializer(BuiltinType.AINT64);
    @SuppressWarnings("unchecked")
    private ISerializerDeserializer<ARecord> recordSerDes = AqlSerializerDeserializerProvider.INSTANCE
            .getSerializerDeserializer(MetadataRecordTypes.EXTERNAL_FILE_RECORDTYPE);

    public ExternalFileTupleTranslator(boolean getTuple) {
        super(getTuple, MetadataPrimaryIndexes.EXTERNAL_FILE_DATASET.getFieldCount());
    }

    @Override
    public ExternalFile getMetadataEntityFromTuple(ITupleReference tuple) throws MetadataException, IOException {
        byte[] serRecord = tuple.getFieldData(EXTERNAL_FILE_PAYLOAD_TUPLE_FIELD_INDEX);
        int recordStartOffset = tuple.getFieldStart(EXTERNAL_FILE_PAYLOAD_TUPLE_FIELD_INDEX);
        int recordLength = tuple.getFieldLength(EXTERNAL_FILE_PAYLOAD_TUPLE_FIELD_INDEX);
        ByteArrayInputStream stream = new ByteArrayInputStream(serRecord, recordStartOffset, recordLength);
        DataInput in = new DataInputStream(stream);
        ARecord externalFileRecord = (ARecord) recordSerDes.deserialize(in);
        return createExternalFileFromARecord(externalFileRecord);
    }

    private ExternalFile createExternalFileFromARecord(ARecord externalFileRecord) {
        String dataverseName = ((AString) externalFileRecord
                .getValueByPos(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_DATAVERSENAME_FIELD_INDEX)).getStringValue();
        String datasetName = ((AString) externalFileRecord
                .getValueByPos(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_DATASET_NAME_FIELD_INDEX)).getStringValue();
        int fileNumber = ((AInt32) externalFileRecord
                .getValueByPos(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_NUMBER_FIELD_INDEX)).getIntegerValue();
        String fileName = ((AString) externalFileRecord
                .getValueByPos(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_NAME_FIELD_INDEX)).getStringValue();
        long fileSize = ((AInt64) externalFileRecord
                .getValueByPos(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_SIZE_FIELD_INDEX)).getLongValue();
        Date lastMoDifiedDate = new Date(
                ((ADateTime) externalFileRecord
                        .getValueByPos(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_MOD_DATE_FIELD_INDEX))
                        .getChrononTime());
        ExternalFilePendingOp pendingOp = ExternalFilePendingOp.values()[((AInt32) externalFileRecord
                .getValueByPos(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_PENDING_OP_FIELD_INDEX))
                .getIntegerValue()];
        return new ExternalFile(dataverseName, datasetName, fileNumber, fileName, lastMoDifiedDate, fileSize, pendingOp);
    }

    @Override
    public ITupleReference getTupleFromMetadataEntity(ExternalFile externalFile) throws MetadataException, IOException {
        // write the key in the first 3 fields of the tuple
        tupleBuilder.reset();
        // dataverse name
        aString.setValue(externalFile.getDataverseName());
        stringSerde.serialize(aString, tupleBuilder.getDataOutput());
        tupleBuilder.addFieldEndOffset();
        // dataset name
        aString.setValue(externalFile.getDatasetName());
        stringSerde.serialize(aString, tupleBuilder.getDataOutput());
        tupleBuilder.addFieldEndOffset();
        // file number
        aInt32.setValue(externalFile.getFileNumber());
        intSerde.serialize(aInt32, tupleBuilder.getDataOutput());
        tupleBuilder.addFieldEndOffset();

        // write the pay-load in the fourth field of the tuple
        recordBuilder.reset(MetadataRecordTypes.EXTERNAL_FILE_RECORDTYPE);

        // write field 0
        fieldValue.reset();
        aString.setValue(externalFile.getDataverseName());
        stringSerde.serialize(aString, fieldValue.getDataOutput());
        recordBuilder.addField(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_DATAVERSENAME_FIELD_INDEX, fieldValue);

        // write field 1
        fieldValue.reset();
        aString.setValue(externalFile.getDatasetName());
        stringSerde.serialize(aString, fieldValue.getDataOutput());
        recordBuilder.addField(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_DATASET_NAME_FIELD_INDEX, fieldValue);

        // write field 2
        fieldValue.reset();
        aInt32.setValue(externalFile.getFileNumber());
        intSerde.serialize(aInt32, fieldValue.getDataOutput());
        recordBuilder.addField(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_NUMBER_FIELD_INDEX, fieldValue);

        // write field 3
        fieldValue.reset();
        aString.setValue(externalFile.getFileName());
        stringSerde.serialize(aString, fieldValue.getDataOutput());
        recordBuilder.addField(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_NAME_FIELD_INDEX, fieldValue);

        // write field 4
        fieldValue.reset();
        aInt64.setValue(externalFile.getSize());
        longSerde.serialize(aInt64, fieldValue.getDataOutput());
        recordBuilder.addField(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_SIZE_FIELD_INDEX, fieldValue);

        // write field 5
        fieldValue.reset();
        aDateTime.setValue(externalFile.getLastModefiedTime().getTime());
        dateTimeSerde.serialize(aDateTime, fieldValue.getDataOutput());
        recordBuilder.addField(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_MOD_DATE_FIELD_INDEX, fieldValue);

        // write field 6
        fieldValue.reset();
        aInt32.setValue(externalFile.getPendingOp().ordinal());
        intSerde.serialize(aInt32, fieldValue.getDataOutput());
        recordBuilder.addField(MetadataRecordTypes.EXTERNAL_FILE_ARECORD_FILE_PENDING_OP_FIELD_INDEX, fieldValue);

        // write record
        try {
            recordBuilder.write(tupleBuilder.getDataOutput(), true);
        } catch (AsterixException e) {
            throw new MetadataException(e);
        }
        tupleBuilder.addFieldEndOffset();

        tuple.reset(tupleBuilder.getFieldEndOffsets(), tupleBuilder.getByteArray());
        return tuple;
    }
}