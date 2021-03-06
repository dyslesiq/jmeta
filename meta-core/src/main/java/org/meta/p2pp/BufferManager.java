/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.p2pp;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ByteBuffer creation for Meta.
 *
 * TODO : it would be nice if we could re-use buffers to avoid creating them each time. Something like the
 * glassfish server memory manager.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public final class BufferManager {

    private static final Logger logger = LoggerFactory.getLogger(BufferManager.class);

    /**
     *
     */
    private BufferManager() {

    }

    /**
     * Creates a direct byte buffer of the given size.
     *
     * @param size the size of the buffer
     * @return the created byte buffer
     */
    public static ByteBuffer aquireDirectBuffer(final int size) {
        return ByteBuffer.allocateDirect(size);
    }

    /**
     * Creates a byte buffer of the given size.
     *
     * @param size the size of the buffer
     * @return the created byte buffer
     */
    public static ByteBuffer aquireBuffer(final int size) {
        return ByteBuffer.allocate(size);
    }

    /**
     * <p>release</p>
     *
     * @param buffer the buffer to release
     */
    public static void release(final ByteBuffer buffer) {
        logger.debug("Releasing buffer with limit/capacity: " + buffer.limit() + "/" + buffer.capacity());
    }

}
