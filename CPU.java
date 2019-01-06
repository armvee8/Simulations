/**

 * Simulates an ARMv8 CPU following the datapath from Figure 4.23 in the textbook.
 *
 * @author Co-Written by Veronica Weiss and Esteban Acosta 
 */
import java.io.*;
import java.util.*;

public class CPU {

	/** Flag to inidcate whether or not in grading mode. Grading mode prints additional messages. */
	private static final boolean GRADING = true;

	/** Memory unit for instructions */
	private Memory instructionMemory;

	/** Memory unit for data */
	private Memory dataMemory;

	/** Register unit */
	private Registers registers;

	/** Arithmetic and logic unit */
	private ALU alu;

	/** Adder for incrementing the program counter */
	private ALU adderPC;

	/** Adder for computing branches */
	private ALU adderBranch;

	/** Control unit */
	private SimpleControl control;

	/** Multiplexor output connects to Read Register 2 */
	private Multiplexor2 muxRegRead2;

	/** Mulitplexor ouptut connects to ALU input B */
	private Multiplexor2 muxALUb;

	/** Multiplexor output connects to Register Write Data */
	private Multiplexor2 muxRegWriteData;

	/** Multiplexor output connects to Program Counter */
	private Multiplexor2 muxPC;

	/** Program counter */
	private boolean[] pc;

	/**
	 * 
	 *
	 * Constructor initializes all data members.
	 *
	 * @param iMemFile Path to the file with instruction memory contents.
	 * @param dMemFile Path to the file with data memory contents.
	 * @exception FileNotFoundException if a file cannot be opened.
	 */
	public CPU(String iMemFile, String dMemFile) throws FileNotFoundException {

		// Create objects for all data members
		instructionMemory = new Memory(iMemFile);
		dataMemory = new Memory(dMemFile);
		registers = new Registers();
		alu = new ALU();
		control = new SimpleControl();
		muxRegRead2 = new Multiplexor2(5);
		muxALUb = new Multiplexor2(32);
		muxRegWriteData = new Multiplexor2(32);
		muxPC = new Multiplexor2(32);


		// Activate adderPC with ADD operation, and inputB set to 4
		// Send adderPC output to muxPC input 0
		adderPC = new ALU();
		adderPC.setControl(2);
		boolean[] four = Binary.uDecToBin(4L);
		adderPC.setInputB(four);

		// Initalize adderBranch with ADD operation
		adderBranch = new ALU();
		adderBranch.setControl(2);

		// initialize program counter to 0
		pc = new boolean[32];
		for(int i = 0; i < 32; i++) {
			pc[i] = false;
		}
	}

	//create a 32 bit array for the sign extend
	boolean [] signExtended = new boolean [32];

	/**
	 * STUDENT SHOULD NOT MODIFY THIS METHOD
	 *
	 * Runs the CPU (fetch, decode, execute cycle). Stops when a halt instruction
	 * is reached.
	 */
	public void run() throws FileNotFoundException {

		boolean[] instruction = fetch();
		boolean op = decode(instruction);

		// Loop until a halt instruction is decoded
		while(op) {
			execute();
			instruction = fetch();
			op = decode(instruction);
		}

		if(GRADING) {
			// Write memory contents to a file
			dataMemory.writeToFile("checkMem.txt");
		}
	}

	/**
	 * STUDENT MUST COMPLETE THIS METHOD
	 *
	 * Fetch the instruction from the instruction memory starting at address pc.
	 *
	 * @return The instruction starting at address pc
	 */
	private boolean[] fetch() {
		boolean instruction[] = new boolean[32]; 
		adderPC.setInputA(pc);
		adderPC.activate();
		instruction = instructionMemory.read32(pc); 
		return instruction; 
	} 

	/**
	 * STUDENT MUST COMPLETE THIS METHOD
	 *
	 * Decode the instruction. Sets the control lines and sends appropriate bits
	 * from the instruction to various inputs within the processor.
	 *
	 * @param instruction The 32-bit instruction to decode
	 * @return false if the opcode is HLT; true for any other opcode
	 */
	private boolean decode(boolean[] instruction) {
		//This array will store the OPCodes for both R format instructions and for the LDR and STR instructions
		boolean [] opCode = new boolean [11];
		//Keep track of where we are in the OPcode array
		int count = 0;
		for(int i = 21; i < 32; i++){
			opCode [count] = instruction[i];
			count++;
}

//Reset the position every time you're loading the instruction OPCode into a new array
count = 0;
/*Since the B and CBZ instructions have a different number of bits for their OPCodes
We need to create two different arrays that store the CBZ OPCode(8 bits) and the B OPCode(6 bits)*/
boolean [] b = new boolean [6];
for(int i = 26; i < 32; i++){

	b[count] = instruction[i];

	count++;

	}
//reset the position every time you're loading the instruction OPCode into a new array
count= 0;
boolean [] cbz = new boolean [8];
for(int i = 24; i < 32; i++){
	cbz[count] = instruction[i];
	count++;
}
int position = 0;
//Load bits 5 to 10 into fiveToNine array
boolean [] fiveToNine = new boolean [5];
for(int i = 5; i< 10 ; i++){
fiveToNine[position] = instruction[i];
position++;
}

//Set the fiveToNine register as the first read register
registers.setRead1Reg(fiveToNine);
//Load bits 16-20 into sixteenToTwenty array
position = 0; 
boolean [] sixteenToTwenty = new boolean[5];
for(int i = 16; i< 21 ; i++){
	sixteenToTwenty[position] = instruction[i];
	position++;

	}

//Loads the bits into zeroToFour array

position = 0;


boolean [] zeroToFour = new boolean [5];
for(int i = 0 ; i < 5; i++){
	zeroToFour [position] = instruction[i];
	position++;
	}
//Set the zeroToFour array as the write reg number

registers.setWriteRegNum(zeroToFour);
//set the zeroToFour array as input one of the multiplexor
muxRegRead2.setInput1(zeroToFour);
//set the sixteenToTwenty as input zero of the multiplexor
muxRegRead2.setInput0(sixteenToTwenty);
//Send the 0-4 bits to write register
registers.setWriteRegNum(zeroToFour);
if(Arrays.equals(Opcode.CBZ, cbz)){

	control.Reg2Loc= true;

	control.ALUSrc = false;

	control.RegWrite= false;

	control.MemRead = false;

	control.MemWrite= false;

	control.Branch = true;
	int index  = 0 ;
//Store all the values of the immediate into the 32 bit array

for(int i = 5 ; i < 24 ;i++){

	signExtended[index] = cbz[i]; 

	index++;

	}


//take the most significant bit of the cbz opcode and continue adding it to the

//bit array until we've filled up the array

for(int i = 24 ; i < 32; i++){

	signExtended[index] = cbz[23];

	index++;

}


//Set the 32 bit immediate value to be input 1 to the multiplexor ALUb

muxALUb.setInput1(signExtended);


//Set the signExtended as adder branch's input B

adderBranch.setInputB(signExtended);


//Set the second register depending on the value of the Reg2Loc control line

registers.setRead2Reg(muxRegRead2.output(control.Reg2Loc));



}


else if(Arrays.equals(Opcode.B,b)){


//Store all the values of the immediate into the 32 bit array

for(int i = 0 ; i < 26 ;i++){

signExtended[i] = b[i]; 

}


//take the most significant bit of the b opcode and continue adding it to bit array until we've filled up the array

for(int i = 26 ; i < 32; i++){

signExtended[i] = b[25];

}

//Set the 32 bit immediate value to be input 1 to the multiplexor

muxALUb.setInput1(signExtended);


//Set the second register depending on the value of the Reg2Loc control line

registers.setRead2Reg(muxRegRead2.output(control.Reg2Loc));

}


else if(Arrays.equals(Opcode.ADD,opCode) || Arrays.equals(Opcode.SUB,opCode) || Arrays.equals(Opcode.ORR,opCode) || Arrays.equals(Opcode.AND,opCode)){


//Since the control lines are the same for these 4 instructions, we only have to write this code once

control.Reg2Loc= false;

control.ALUSrc = false;

control.MemtoReg= false;

control.RegWrite= true;

control.MemRead = false;

control.MemWrite= false;

control.Branch = false;


//however, there is one control line that will be different for each instruction

//so, if the instruction is ADD, then we must set the AlUControl to 2

if(Arrays.equals(Opcode.ADD,opCode)){

control.ALUControl  = 2;

}


//if the instruction is SUB, then we must set the AlUControl to 6

else if( Arrays.equals(Opcode.SUB,opCode)){

control.ALUControl  = 6;

}


//if the instruction is ORR, then we must set the AlUControl to 1

else if(Arrays.equals(Opcode.ORR,opCode) ){

control.ALUControl  = 1;

}


//if the instruction is AND, then we must set the AlUControl to 0

else { 

control.ALUControl  = 0;

}


//Set the second register depending on the value of the Reg2Loc control line

registers.setRead2Reg(muxRegRead2.output(control.Reg2Loc));


}


else if(Arrays.equals(Opcode.STR,opCode)){


//set the control lines for STR

control.Reg2Loc= true;

control.ALUSrc = true;

control.RegWrite= false;

control.MemRead = false;

control.MemWrite= true;

control.Branch = false;


int index  = 0 ;

//Store all the values of the immediate into the 32 bit array

for(int i = 12 ; i < 21 ;i++){

signExtended[index] = opCode[i]; 

index++;

}


//take the most significant bit of the STR opcode and continue adding it to the array until

//we've filled up the array

for(int i = 21 ; i < 32; i++){

signExtended[index] = opCode[20];

index++;

}


//Set the 32 bit immediate value to be input 1 to the multiplexor RegWriteData

muxALUb.setInput1(signExtended);


//Set the 32 bit immediate value to be input B to the ALU

adderBranch.setInputB(signExtended);


//Set the second register depending on the value of the Reg2Loc control line

registers.setRead2Reg(muxRegRead2.output(control.Reg2Loc));



//decoded = true;


}


else if(Arrays.equals(Opcode.LDR,opCode)){


control.ALUSrc = true;

control.MemtoReg= true;

control.RegWrite= true;

control.MemRead = true;

control.MemWrite= false;

control.Branch = false;



int index  = 0 ;

//Store all the values of the immediate into the 32 bit array

for(int i = 12 ; i < 21 ;i++){

signExtended[index] = opCode[i]; 

index++;

}


//take the most significant bit of the LDR opcode and continue adding it to the array until

//we've filled up the 32 bit array

for(int i = 21 ; i < 32; i++){

signExtended[index] = opCode[20];

index++;

}

//Set the 32 bit immediate value to be input 1 to the multiplexor RegWriteData

muxALUb.setInput1(signExtended);


//Set the 32 bit immediate value to be input B to the ALU

adderBranch.setInputB(signExtended);


//Set the second register depending on the value of the Reg2Loc control line

registers.setRead2Reg(muxRegRead2.output(control.Reg2Loc));



}


/*else if(Arrays.equals(Opcode.HLT, opCode)){

decoded = false;

}*/

for(int i = (instruction.length - Opcode.HLT.length),j=0; i < instruction.length-1; i++,j++){
	if(instruction[i] != Opcode.HLT[j]){
		break;
	}
	else if(i == instruction.length-2){
		return false;
	}
}

return true;

}




	/**
	 * STUDENT MUST COMPLETE THIS METHOD
	 *
	 * Execute the instruction.
	 *
	 */
	//boolean[] registerValue = registers.getReadReg1(); (this will need to go into the ALU) 

	private void execute() {
		//Read the data that is in register 1 and store it as the first input of the ALU
		alu.setInputA(registers.getReadReg1());
		//Set the second register data as input 0 of the multiplexor
		muxALUb.setInput0(registers.getReadReg2());
		//Set the sign extend as input 1 of the multiplexor
		muxALUb.setInput1(signExtended);
		//If the ALU Src is true, then set the sign extended bit array as the ALU's input B
		if(control.ALUSrc){
			alu.setInputB(muxALUb.output(control.ALUSrc));
		}
		//Set the ALU control line to determine what operation we are going to perform
		alu.setControl(control.ALUControl); 
		//Activate the ALU
		alu.activate();
		//Make the first input of the multiplexor the ALU output
		muxRegWriteData.setInput0(alu.getOutput());
		//if the control line to read from memory is true, then
		if(control.MemRead){
			//Read the ALU's output address and set it as the multiplexor's first input
			//Use dataMemory.read32() to read from memory
			muxRegWriteData.setInput1(dataMemory.read32(alu.getOutput()));
		}
		//If the control value MemToReg is true
		if(control.MemtoReg){
			//We set the register's write data s the multiplexor's input 1 value
			registers.setWriteRegData(muxRegWriteData.output(control.MemtoReg));
		}
		//If the control value MemToReg is false
		else{
			//We set the register's as the multiplexor's input 0 value
			registers.setWriteRegData(muxRegWriteData.output(control.MemtoReg)); 
		}
		//If the memWrite control line is true
		if(control.MemWrite){
			//Use DataMemory.write32() to write from memory
			dataMemory.write32(alu.getOutput(), registers.getReadReg2());
		}
		if(control.RegWrite){
			registers.activateWrite();
		}
		//Writing value to register 
		muxPC.setInput0(pc);
		adderBranch.activate();	
		//Write value to register (if control.RegWrite is true)
		if(control.RegWrite){
			registers.activateWrite();
		}
		//Write the new value to the program counter (pc) 
		//Sets Input 0 of muxPC
		muxPC.setInput0(alu.getOutput());
		//Sets input 1 of muxPC to the output from adderBranch 
		muxPC.setInput1(adderBranch.getOutput());
		//Uses control signals and zero flag from ALU 
		//to select output for muxPC
		//Logic from Figure 4.23 
		boolean logicOutput = true;
		if(control.Uncondbranch == true && alu.getZeroFlag() == true){
			//if both numbers going into the AND gate are 1, 
			//then the OR will contain at least a 1 so the output will always be true
			//and the output gets put into pc Mux's 1 input
			logicOutput = true;
			muxPC.output(true);
		}
		else if(control.Branch == false && alu.getZeroFlag() == false){
			muxPC.output(false);
		}
		//If 
		else{
			muxPC.output(true);
		}
		//Set pc data member to the output from muxPC 
		pc = muxPC.output();
	}
}
