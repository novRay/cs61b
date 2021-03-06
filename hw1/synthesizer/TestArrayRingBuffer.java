package synthesizer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/** Tests the ArrayRingBuffer class.
 *  @author Josh Hug
 */

public class TestArrayRingBuffer {
    @Test
    public void someTest() {
        ArrayRingBuffer<Integer> arb = new ArrayRingBuffer(3);
        arb.enqueue(1);
        arb.enqueue(2);
        arb.enqueue(3);
        assertEquals(3, arb.fillCount());
        assertEquals(1, (int)arb.dequeue());
        assertEquals(2, (int)arb.peek());
        assertEquals(2, (int)arb.dequeue());
        assertEquals(3, (int)arb.peek());
        arb.enqueue(4);
        assertFalse(arb.isFull());
        arb.enqueue(5);
        assertTrue(arb.isFull());
        assertEquals(7, (int)arb.dequeue() + arb.peek());
    }

    /** Calls tests for ArrayRingBuffer. */
    public static void main(String[] args) {
        jh61b.junit.textui.runClasses(TestArrayRingBuffer.class);
    }
} 
