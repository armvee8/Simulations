// Veronica Weiss, Esteban Acosta 
// CS318 Programming Assignment 3
// Spring 2018
// A64 implementation of Binary Search Tree
	.align 2
	.data
	// Assume that the BST's are full and complete
	// (every node other than the leaves has exactly two children, leaves are all
	// at the same depth).
	// Structure of the tree data:
	// First value is the number of nodes in the tree.
	// This is followed by the values stored in each node. The BST is stored as
	// an array where the childern of the node at index i are located at indexes
	// (2i+1) and (2i+2).
treeA: // height is 3
	.dword 15 // number of nodes in treeA
	.dword 57,39,72,23,50,62,87,20,27,49,53,60,63,81,95 // BST represented as an array
treeB: // height is 5
	.dword 63 // number of nodes in treeB
	// BST represented as an array
	.dword 2941,1836,3400,1418,2176,3298,4199,1128,1472,2143
    .dword 2552,3060,3310,3598,4280,1020,1150,1438,1713,2037
    .dword 2154,2219,2634,2987,3104,3305,3362,3487,3674,4242
    .dword 4733,1009,1057,1146,1223,1426,1453,1663,1755,1962
    .dword 2079,2145,2175,2189,2379,2602,2654,2974,3012,3095
    .dword 3162,3300,3307,3325,3373,3458,3511,3632,3912,4222
    .dword 4278,4673,4947
treeC: // empty tree, height procedure should return -1
	.dword 0 // number of nodes in treeC
treeD: // tree has one node, height procedure should return 0
	.dword 1 // number of nodes in treeD
	.dword 12345 // single node in the tree
	.text
	.global main
main:

	////////////////////
	// Test 1: treeA
	// Call the height procedure
	ADR X1,treeA // Put the base memory address of the tree into X1
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	BL p_height

	//Call the search procedure
	MOV X1,#87 // key value to search for
	ADR X2,treeA // base memory address of the tree
	ADD X2,X2,#8 // before calling the procedure, put address of first array element into X2
	MOV X3,#0 // X3 = array index of root node
	MOV X4,#0 // X4 = offset of root node
	BL p_search

	////////////////////
	// Test 2: treeB
	// Call the height procedure
	ADR X1,treeB // Put the base memory address of the tree into X1
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	BL p_height

	// Call the search procedure
	MOV X1,#2189 // key value to search for
	ADR X2,treeB // base memory address of the tree
	ADD X2,X2,#8 // before calling the procedure, put address of first array element into X2
	MOV X3,#0 // X3 = array index of root node
	MOV X4,#0 // X4 = offset of root node
	BL p_search

	////////////////////
	// Test 3: treeC
	// Call the height procedure
	ADR X1,treeC // Put the base memory address of the tree into X1
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	BL p_height

	// Call the search procedure
	MOV X1,#987 // key value to search for
	ADR X2,treeC // base memory address of the tree
	ADD X2,X2,#8 // before calling the procedure, put address of first array element into X2
	MOV X3,#0 // X3 = array index of root node
	MOV X4,#0 // X4 = offset of root node
	BL p_search

	////////////////////
	// Test 4: treeD
	// Call the height procedure
	ADR X1,treeD // Put the base memory address of the tree into X1
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	BL p_height

	// Call the search procedure
	MOV X1,#12345 // key value to search for
	ADR X2,treeD // base memory address of the tree
	ADD X2,X2,#8 // before calling the procedure, put address of first array element into X2
	MOV X3,#0 // X3 = array index of root node
	MOV X4,#0 // X4 = offset of root node
	BL p_search


	// End of main procedure, branch to end of program
	B program_end

p_height:
	// Height Procedure (iterative implementation)
	// X0: Returns the height of the tree (number of edges from root to deepest leaf).
	// If the tree is empty, returns -1; if the tree contains 1 node, returns 0.
	// X1: The memory base address of the binary search tree. Assumes the value before this
	// memory address is the number of nodes in the BST, followed by the values in each node
	// of the BST. Assumes the BST is full and complete (procedure will not alter)
	//
	// This procedure must use an iterative (non-recursive) algorithm. The performance
	// of the solution must be O(log n), where n is the number of nodes in the tree.
	//
	// Temporary registers used by this procedure:
	// <student must list the registers; start with X9, and use registers in number order
	// as needed up to X15>
	// Values of the temporary registers used by this procedure must be preserved.

	SUB SP, SP, #16 //places the stack pointer at the top
	STR X10, [SP, #8] //X10 is our temporary value

	LDR X9, [X1, #-8] //grabs the first value (number of nodes) in the tree
	MOV X10, #0 //Position in the array
	MOV X0 , #-1 // Height of the BST

	//If tree is empty, returns -1
	SUB X9, X9 ,X10
	CBZ X9 , empty_tree

	//If the tree contains one node, return 0
	SUB X9, X9, #1
	CBZ X9, one_node

	loop_top:
	//Make sure the values are the same
	SUB X9, X9, X10

	//If they are, break out of the loop
	CBZ X9, loop_end

	//Otherwise, load the total number of nodes back into X9
	LDR X9 , [X1, #-8]

	//Increment height by one
	ADD X0, X0, #1

	//Multiply the index position by two
	ADD X10, X10, X10

	//and add X15 by 1
	ADD X10, X10, #1 //needs to grab X15 * 8th place

	//loop again
	B loop_top

	//stores height in this function

	one_node:
	MOV X0, #0
	B loop_end

	empty_tree:
	MOV X0, #-1
	B loop_end

	loop_end:
	LDR X10, [SP, #8]
	ADD SP, SP, #16
	BR X30
	// End of height procedure

p_search:
	// Search Procedure (recursive implementation)
	// X0: Returns the array index where the key is found, or -1 if the key is not found.
	// X1: The key value to search for (procedure will not alter)
	// X2: The memory base address of the binary search tree. Assumes the value before this
	// memory address is the number of nodes in the BST, followed by the values in each node
	// of the BST. Assumes the BST is full and complete (procedure will not alter)
	// X3: The index of the current sub-tree root (procedure may alter)
	// X4: The memory offset of the current sub-tree root (procedure may alter)
	//
	// This procedure must use a recursive algorithm that has worst case
	// performance O(tree height).
	//
	// Temporary registers used by this procedure:
	// <student must list the registers; start with X9, and use registers in number order
	// as needed up to X15>
	// Values of the temporary registers used by this procedure must be preserved.

	SUB SP, SP, #32
	STR X9, [SP, #24]
	STR X10, [SP, #16]
	STR X30, [SP,#8]
	//x3 - position
	//x4 - offset

	LDR X9, [X2, X4] //loads current value into X9 ( X9 = current value)
	SUBS X10, X1, X9 //if current value = value to search for (X10 stores the result of X1-19)
	CBZ X10, found // branch out

	B.LT left // if the search value is smaller than the current value, branch to the left label (go to the left child)

	B.GT right // if the search value is greater than the current value, branch to the right label (go to the right child)

	left:
	//Left child position
	ADD X3, X3, X3
	ADD X3, X3, #1

	//Load the # of elements in the array into x9
	LDR X9,[X2, #-8]

	//check to see if we are out of bounds
	SUBS X10, X3, X9

	//if the current position is greater than array length then we branch out to the non found label
	B.GT not_found

	//move offset
	ADD X4, X4, X4
	ADD X4, X4, #8
	LDR X9, [X2, X4]

	B p_search

	right:
	ADD X3, X3, X3
	ADD X3, X3, #2
	//Load the # of elements in the array into x9
	LDR X9,[X2, #-8]

	//check to see if we are out of bounds
	SUBS X10, X3, X9

	//If the current position is greater than array length then we branch out to the non found label
	B.GT not_found

	//move offset
	ADD X4, X4, X4
	ADD X4, X4, #16
	LDR X9, [X2, X4]
	B  p_search

	found:
	MOV X0, X3
	B recurse_end

	not_found:
	MOV X0, #-1
	B recurse_end

	recurse_end:
	LDR X30, [SP, #8]
	LDR X10, [SP, #16]
	LDR X9, [SP, #24]
	ADD SP, SP, #32
	BR X30



	// End of search procedure

program_end:
	MOV X15,#0 // placeholder at end of program
	.end
