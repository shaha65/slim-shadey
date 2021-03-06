package opal.structures;

import opal.exceptions.UnderflowException;
import javafx.scene.image.Image;
//PairingHeap class
//
// CONSTRUCTION: with no initializer
//
// ******************PUBLIC OPERATIONS*********************
// Position insert( x )   --> Insert x, return position
// Comparable deleteMin( )--> Return and remove smallest item
// void delete( HeapNode p )--> remove the indicated item     *** Added by TW, but commented out at the bottom***
// Comparable findMin( )  --> Return smallest item
// boolean isEmpty( )     --> Return true if empty; else false
// int size( )            --> Return size of priority queue
// void makeEmpty( )      --> Remove all items
// void decreaseKey( HeapNode p, newVal )
//                        --> Decrease value in node p   *** not used, so commented out at the bottom ***
// ******************ERRORS********************************
// Exceptions thrown for various operations

// NOTES:
// downloaded from http://www.cs.fiu.edu/~weiss/dsj2/code/weiss/nonstandard/PairingHeap.java
// Modified slightly, 
//    * Added my own function, "delete( Position p)"
//    * removed the dependence on the PriorityQueue interface (this is the only one I'm using)
//    * pulled HeapNode out as a public class, since mergeTree nodes keep pointers to them for purposes of calls to "delete"


/**
 * Implements a pairing heap.
 * Supports a decreaseKey operation.
 * Note that all "matching" is based on the compareTo method.
 * @author Mark Allen Weiss
 * @see PriorityQueue.Position
 */
public class PairingHeap 
{    
    /**
     * Construct the pairing heap.
     */
    public PairingHeap( )
    {
        root = null;
        theSize = 0;
    }

    /**
     * Insert into the priority queue, and return a Position
     * that can be used by decreaseKey.
     * Duplicates are allowed.
     * @param x the item to insert.
     * @return the node containing the newly inserted item.
     */
    public HeapNode insert( Comparable x )
    {
        HeapNode newNode = new HeapNode( x );

        if( root == null )
            root = newNode;
        else
            root = compareAndLink( root, newNode );
            
        theSize++;
        return newNode;
    }

    /**
     * Find the smallest item in the priority queue.
     * @return the smallest item.
     * @throws UnderflowException if pairing heap is empty.
     */
    public Comparable findMin( )
    {
        if( isEmpty( ) )
            throw new UnderflowException( "Pairing heap is empty" );
        return root.element;
    }

    /**
     * Remove the smallest item from the priority queue.
     * @return the smallest item.
     * @throws UnderflowException if pairing heap is empty.
     */
    public Comparable deleteMin( )
    {
    	if( isEmpty( ) )
            throw new UnderflowException( "Pairing heap is empty" );

        Comparable x = findMin( );
        if( root.leftChild == null )
            root = null;
        else
            root = combineSiblings( root.leftChild );

        theSize--;
        return x;
    }

    
    


    /**
     * Test if the priority queue is logically empty.
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty( )
    {
        return root == null;
    }

    /**
     * Returns number of items stored in the priority queue.
     * @return size of the priority queue.
     */
    public int size( )
    {
        return theSize;
    }

    /**
     * Make the priority queue logically empty.
     */
    public void makeEmpty( )
    {
        root = null;
        theSize = 0;
    }
    

    private HeapNode root;
    private int theSize;

    /**
     * Internal method that is the basic operation to maintain order.
     * Links first and second together to satisfy heap order.
     * @param first root of tree 1, which may not be null.
     *    first.nextSibling MUST be null on entry.
     * @param second root of tree 2, which may be null.
     * @return result of the tree merge.
     */
    private HeapNode compareAndLink( HeapNode first, HeapNode second )
    {
        if( second == null )
            return first;

        if( second.element.compareTo( first.element ) < 0 )
        {
            // Attach first as leftmost child of second
            second.prev = first.prev;
            first.prev = second;
            first.nextSibling = second.leftChild;
            if( first.nextSibling != null )
                first.nextSibling.prev = first;
            second.leftChild = first;
            return second;
        }
        else
        {
            // Attach second as leftmost child of first
            second.prev = first;
            first.nextSibling = second.nextSibling;
            if( first.nextSibling != null )
                first.nextSibling.prev = first;
            second.nextSibling = first.leftChild;
            if( second.nextSibling != null )
                second.nextSibling.prev = second;
            first.leftChild = second;
            return first;
        }
    }

    private HeapNode [ ] doubleIfFull( HeapNode [ ] array, int index )
    {
        if( index == array.length )
        {
            HeapNode [ ] oldArray = array;

            array = new HeapNode[ index * 2 ];
            for( int i = 0; i < index; i++ )
                array[ i ] = oldArray[ i ];
        }
        return array;
    }
   
        // The tree array for combineSiblings
    private HeapNode [ ] treeArray = new HeapNode[ 5 ];

    /**
     * Internal method that implements two-pass merging.
     * @param firstSibling the root of the conglomerate;
     *     assumed not null.
     */
    private HeapNode combineSiblings( HeapNode firstSibling )
    {
        if( firstSibling.nextSibling == null )
            return firstSibling;

            // Store the subtrees in an array
        int numSiblings = 0;
        for( ; firstSibling != null; numSiblings++ )
        {
            treeArray = doubleIfFull( treeArray, numSiblings );
            treeArray[ numSiblings ] = firstSibling;
            firstSibling.prev.nextSibling = null;  // break links
            firstSibling = firstSibling.nextSibling;
        }
        treeArray = doubleIfFull( treeArray, numSiblings );
        treeArray[ numSiblings ] = null;

            // Combine subtrees two at a time, going left to right
        int i = 0;
        for( ; i + 1 < numSiblings; i += 2 )
            treeArray[ i ] = compareAndLink( treeArray[ i ], treeArray[ i + 1 ] );

        int j = i - 2;

            // j has the result of last compareAndLink.
            // If an odd number of trees, get the last one.
        if( j == numSiblings - 3 )
            treeArray[ j ] = compareAndLink( treeArray[ j ], treeArray[ j + 2 ] );

            // Now go right to left, merging last tree with
            // next to last. The result becomes the new last.
        for( ; j >= 2; j -= 2 )
            treeArray[ j - 2 ] = compareAndLink( treeArray[ j - 2 ], treeArray[ j ] );

        return treeArray[ 0 ];
    }

    private class HeapNode {
        /**
         * Construct the HeapNode.
         * @param theElement the value stored in the node.
         */
        public HeapNode( Comparable theElement )
        {
            element     = theElement;
            leftChild   = null;
            nextSibling = null;
            prev        = null;
        }

        /**
         * Returns the value stored at this position.
         * @return the value stored at this position.
         */
        public Comparable getValue( )
        {
            return element;
        }
        
            // Friendly data; accessible by other package routines
        public Comparable element;
        public HeapNode   leftChild;
        public HeapNode   nextSibling;
        public HeapNode   prev;
    }

    
}


/*
 * Not used, so I've removed it from the code
 * 
 * Change the value of the item stored in the pairing heap.
 * @param pos any Position returned by insert.
 * @param newVal the new value, which must be smaller
 *    than the currently stored value.
 * @throws IllegalArgumentException if pos is null.
 * @throws IllegalValueException if new value is larger than old.
 *
/*public void decreaseKey( HeapNode pos, Comparable newVal )
{
    if( pos == null )
        throw new IllegalArgumentException( "null Position passed to decreaseKey" );

    HeapNode p = (HeapNode) pos;
    
    if( p.element.compareTo( newVal ) < 0 )
        throw new IllegalValueException( "newVal/oldval: " + newVal + " /" + p.element );
    p.element = newVal;
    if( p != root )
    {
        if( p.nextSibling != null )
            p.nextSibling.prev = p.prev;
        if( p.prev.leftChild == p )
            p.prev.leftChild = p.nextSibling;
        else
            p.prev.nextSibling = p.nextSibling;

        p.nextSibling = null;
        root = compareAndLink( root, p );
    }
}*/


/*  
 * I decided not to use this, but am keeping it in case I change my mind 
 *
 * Remove the indicated item from the priority queue.
 * @throws UnderflowException if pairing heap is empty.
 *
/*public void delete( HeapNode node  )
{
    if( isEmpty( ) )
        throw new UnderflowException( "Pairing heap is empty" );

    if( node == null )
        throw new IllegalArgumentException( "null Position passed to decreaseKey" );

    if (node == root ) {
    	deleteMin();
    } else {
    	//find node's last child, and link it to node's sibling
    	HeapNode lastChild = node.leftChild;
    	if (lastChild == null) { //leaf,  so link prev directly to next sibling
        	if (node.prev.leftChild == node)  // node is the first child in a list
        		node.prev.leftChild = node.nextSibling;
        	else // node has a preceding sibling
        		node.prev.nextSibling = node.nextSibling;
    		
    	} else { //not a leaf
    		while (lastChild.nextSibling != null)
    			lastChild = lastChild.nextSibling;
    		lastChild.nextSibling = node.nextSibling;

    		//move node's left child to take over node's place in line 
        	if (node.prev.leftChild == node)  // node is the first child in a list
        		node.prev.leftChild = node.leftChild;
        	else // node has a preceding sibling
        		node.prev.nextSibling = node.leftChild;

    	}
        theSize--;
    }        	
}
    
*/

// Test program
/*public static void main( String [ ] args )
{
    PairingHeap h = new PairingHeap( );
    int numItems = 10000;
    int i = 37;
    int j;

    System.out.println( "Checking; no bad output is good" );
    for( i = 37; i != 0; i = ( i + 37 ) % numItems )
       h.insert( new Integer( i ) );
    for( i = 1; i < numItems; i++ )
        if( ((Integer)( h.deleteMin( ) )).intValue( ) != i )
            System.out.println( "Oops! " + i );

    PriorityQueue.Position [ ] p = new PriorityQueue.Position[ numItems ];
    for( i = 0, j = numItems / 2; i < numItems; i++, j =(j+71)%numItems )
        p[ j ] = h.insert( new Integer( j + numItems ) );
    for( i = 0, j = numItems / 2; i < numItems; i++, j =(j+53)%numItems )
        h.decreaseKey( p[ j ], new Integer( 
                 ((Integer)p[ j ].getValue( )).intValue( ) - numItems ) );
    i = -1;
    while( !h.isEmpty( ) )
        if( ((Integer)( h.deleteMin( ) )).intValue( ) != ++i )
            System.out.println( "Oops! " + i + " " );
    System.out.println( "Check completed" );
}*/
