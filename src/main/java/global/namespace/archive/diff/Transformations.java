/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.delta.model.EntryNameAndDigestValue;
import global.namespace.archive.delta.model.EntryNameAndTwoDigestValues;

/**
 * Transforms an object into an
 * {@link EntryNameAndDigestValue} by applying
 * some dark magic.
 *
 * @param <T> the type of the objects to transform.
 * @author Christian Schlichtherle
 */
interface Transformation<T> {

    EntryNameAndDigestValue apply(T item);
}

/**
 * The identity transformation.
 *
 * @author Christian Schlichtherle
 */
final class IdentityTransformation implements Transformation<EntryNameAndDigestValue> {

    @Override
    public EntryNameAndDigestValue apply(EntryNameAndDigestValue entryNameAndDigestValue) {
        return entryNameAndDigestValue;
    }
}

/**
 * Selects the second entry name and digest from the given entry name and
 * two digests.
 *
 * @author Christian Schlichtherle
 */
final class EntryNameAndDigest2Transformation implements Transformation<EntryNameAndTwoDigestValues> {

    @Override
    public EntryNameAndDigestValue apply(EntryNameAndTwoDigestValues entryNameAndTwoDigestValues) {
        return entryNameAndTwoDigestValues.secondEntryNameAndDigestValue();
    }
}
