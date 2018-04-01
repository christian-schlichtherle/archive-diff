package global.namespace.archive.diff.model.dto;

import global.namespace.archive.diff.model.DeltaModel;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.security.MessageDigest;

public final class DeltaModelAdapter extends XmlAdapter<DeltaModelDTO, DeltaModel> {

    private final EntryNameAndDigestValueCollectionAdapter
            entryNameAndDigestValueCollectionAdapter = new EntryNameAndDigestValueCollectionAdapter();

    private final EntryNameAndTwoDigestValuesCollectionAdapter
            entryNameAndTwoDigestValuesCollectionAdapter = new EntryNameAndTwoDigestValuesCollectionAdapter();

    @Override
    public DeltaModel unmarshal(DeltaModelDTO v) throws Exception {
        if (null == v) {
            return null;
        } else {
            return DeltaModel
                    .builder()
                    .messageDigest(MessageDigest.getInstance(v.algorithm))
                    .changedEntries(entryNameAndTwoDigestValuesCollectionAdapter.unmarshal(v.changed))
                    .unchangedEntries(entryNameAndDigestValueCollectionAdapter.unmarshal(v.unchanged))
                    .addedEntries(entryNameAndDigestValueCollectionAdapter.unmarshal(v.added))
                    .removedEntries(entryNameAndDigestValueCollectionAdapter.unmarshal(v.removed))
                    .build();
        }
    }

    @Override
    public DeltaModelDTO marshal(DeltaModel v) throws Exception {
        final DeltaModelDTO dto = new DeltaModelDTO();
        dto.algorithm = v.digestAlgorithmName();
        dto.numBytes = v.digestByteLength().orElse(null);
        dto.changed = entryNameAndTwoDigestValuesCollectionAdapter.marshal(v.changedEntries());
        dto.unchanged = entryNameAndDigestValueCollectionAdapter.marshal(v.unchangedEntries());
        dto.added = entryNameAndDigestValueCollectionAdapter.marshal(v.addedEntries());
        dto.removed = entryNameAndDigestValueCollectionAdapter.marshal(v.removedEntries());
        return dto;
    }
}
