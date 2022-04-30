// Copyright 2020 Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.wd.core.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUCache<K, V> {

    public class Node {
        public K key;
        public V val;
        public Instant timestamp = Instant.now();
        public Node next;
        public Node prev;

        public Node(K key, V val) {
            this.key = key;
            this.val = val;
        }
    }

    private ConcurrentHashMap<K, Node> _map;
    private ConcurrentLinkedQueue<K> _queue;
    private int _capacity;
    private int _minutes;

    private ReadWriteLock _readWriteLock = new ReentrantReadWriteLock();
    private Lock _readLock = _readWriteLock.readLock();
    private Lock _writeLock = _readWriteLock.writeLock();

    public LRUCache(int capacity, int minutes) {
        _capacity = capacity;
        _minutes = minutes;
        _map = new ConcurrentHashMap<K, Node>();
        _queue = new ConcurrentLinkedQueue<K>();
    }

    private boolean isFull() {
        return _queue.size() >= _capacity;
    }

    public V get(K key) {
        _readLock.lock();
        try {
            if (!_map.containsKey(key))
                return null;

            Node node = _map.get(key);

            if (Duration.between(Instant.now(), node.timestamp).toMinutes() > _minutes) {
                return null;
            }

            _queue.remove(key);
            _queue.add(key);
            return node.val;
        } finally {
            _readLock.unlock();
        }
    }

    public void set(K key, V value) {
        _writeLock.lock();
        try {
            Node node;
            if (_map.containsKey(key)) {
                node = _map.get(key);
                node.val = value;
                node.timestamp = Instant.now();
                _queue.remove(key);
                _queue.add(key);
                return;
            }

            cleanUp();

            if (_queue.size() >= _capacity) {
                _map.remove(_queue.poll());
            }

            node = new Node(key, value);
            _map.put(key, node);
            _queue.add(key);
        } finally {
            _writeLock.unlock();
        }
    }

    public void remove(K key) {
        _writeLock.lock();
        try {
            _writeLock.lock();
            _queue.remove(key);
            _map.remove(key);
        } finally {
            _writeLock.unlock();
        }
    }

    private void cleanUp() {
        Iterator<K> it = _queue.iterator();
        while (it.hasNext()) {
            K key = it.next();
            Node node = _map.get(key);
            if (Duration.between(Instant.now(), node.timestamp).toMinutes() > _minutes) {
                _map.remove(key);
                it.remove();
            }
        }
    }
}
