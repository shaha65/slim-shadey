package opal.align.shapes;

import opal.IO.SequenceConverter;
import javafx.scene.image.Image;

import opal.align.Alignment;
import opal.IO.StructureFileReader;
import opal.align.Aligner;
import opal.align.StructureAlignment;
import opal.IO.Configuration;
import opal.IO.Inputs;

public class ShapeQuadratic extends Shape {

	public ShapeQuadratic(Configuration c, Alignment A, Alignment B) {
		super(c, A, B);
		// TODO Auto-generated constructor stub
	}

	public ShapeQuadratic(Shape s) {
		super(s);
		// TODO Auto-generated constructor stub
	}


	
	 /* procedure: gapOpenCost
	 *   Input:
	 *     a, b: column indices into A and B respectively
	 *        s: integer array representation of a shape
	 *   Output: gc, a int representing the sum of pairwise
	 *           gap startup penalties between A and B (not within A, nor B)
	 *           resulting from appending column a of A and column b of B 
	 *           to shape s (a < 0 indicates deletion, b < 0 indicates insertion).
	 *  This is a O(KL) version.
	 */
	final protected long gapBoundaryCost (int a, int b, Aligner.Direction dir) {
		int i = 0;
		long cost = 0;
		for (i = 0; i < K; i++) { 
			for (int j = 0; j < L; j++) {
				if (Aligner.Direction.horiz == dir)  {
					if (seqBlocks[i] >= seqBlocks[j+K]  &&  SequenceConverter.GAP_VAL != B.seqs[j][b-1]) {
						if(aPos < A.firstLetterLoc[i])
							cost += config.leftGammaTerm();
						else if(aPos >= A.lastLetterLoc[i])
							cost += config.rightGammaTerm();
						else
							cost += config.gamma;
						//cost += (aPos < A.firstLetterLoc[i] || aPos >= A.lastLetterLoc[i]) ? config.gammaTerm : config.gamma;
						if (config.useStructure && aPos > 0) {
							int pos = ((StructureAlignment)A).origSeqIndices[i][aPos-1];
							if (pos>0)
								cost += config.gapOpenMods[ config.getStructureLevelFromProbability( A.in.structure.structureNeighborLevels[A.seqIds[i]][pos] ) ] ;
						}
					}
				} else if (Aligner.Direction.vert == dir)  {
					if (seqBlocks[j+K] >= seqBlocks[i]  &&  SequenceConverter.GAP_VAL != A.seqs[i][a-1]) {
						if(bPos < B.firstLetterLoc[j])
							cost += config.leftGammaTerm();
						else if(bPos >= B.lastLetterLoc[j])
							cost += config.rightGammaTerm();
						else 
							cost += config.gamma;
						//cost += (bPos < B.firstLetterLoc[j] || bPos >= B.lastLetterLoc[j]) ? config.gammaTerm : config.gamma;					
						if (config.useStructure && bPos > 0) {
							int pos = ((StructureAlignment)B).origSeqIndices[j][bPos-1];
							if (pos>0)
								cost += config.gapOpenMods[ config.getStructureLevelFromProbability( B.in.structure.structureNeighborLevels[B.seqIds[j]][pos] ) ] ;
						}
					}
				} else { //if (Aligner.DIAG == dir)
					if ( seqBlocks[i] >= seqBlocks[j+K]  &&  SequenceConverter.GAP_VAL != B.seqs[j][b-1] && SequenceConverter.GAP_VAL == A.seqs[i][a-1]) { 
							if(aPos < A.firstLetterLoc[i])
								cost += config.leftGammaTerm();
							else if(aPos >= A.lastLetterLoc[i])
								cost += config.rightGammaTerm();
							else 
								cost += config.gamma;
							//cost += (aPos < A.firstLetterLoc[i] || aPos >= A.lastLetterLoc[i]) ? config.gammaTerm : config.gamma;						
							if (config.useStructure && aPos > 0) {
								int pos = ((StructureAlignment)A).origSeqIndices[i][aPos-1];
								if (pos>0)
									cost += config.gapOpenMods[ config.getStructureLevelFromProbability( A.in.structure.structureNeighborLevels[A.seqIds[i]][pos] ) ] ;
							}
					} else if (seqBlocks[j+K] >= seqBlocks[i]  &&  SequenceConverter.GAP_VAL != A.seqs[i][a-1] && SequenceConverter.GAP_VAL == B.seqs[j][b-1])  {
							if(bPos < B.firstLetterLoc[j])
								cost += config.leftGammaTerm();
							else if(bPos >= B.lastLetterLoc[j])
								cost += config.rightGammaTerm();
							else 
								cost += config.gamma;
							//cost += (bPos < B.firstLetterLoc[j] || bPos >= B.lastLetterLoc[j]) ? config.gammaTerm : config.gamma;						
							if (config.useStructure && bPos > 0) {
								int pos = ((StructureAlignment)B).origSeqIndices[j][bPos-1];
								if (pos>0)
									cost += config.gapOpenMods[ config.getStructureLevelFromProbability( B.in.structure.structureNeighborLevels[B.seqIds[j]][pos] ) ] ;
							}
					}
				}
			}
		}
		
		return cost;
	}

}
