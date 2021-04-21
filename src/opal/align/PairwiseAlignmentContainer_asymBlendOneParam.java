package opal.align;

import com.traviswheeler.libs.LogWriter;
import javafx.scene.image.Image;

import opal.tree.Tree;
import opal.IO.Configuration;

public class PairwiseAlignmentContainer_asymBlendOneParam extends
		PairwiseAlignmentsContainer {

	
	public PairwiseAlignmentContainer_asymBlendOneParam(Tree tree, float[][] distances, Configuration c) {
		super(tree, distances, c);
	}

	protected void setBlendParams (int neighborCnt) {
		c_weight = neighborCnt==0? 0 : consistency_weight/neighborCnt;
		//gammas?
		normalizer = 1/(PairSuboptimalityMatrices.delta * 1 *(1+ neighborCnt==0? 0 : consistency_weight)); // will make the worse-case cost just double the cost of an unmodified edge	
	}

	protected int calcSub(int a, int b, int i, int j, ConsistencyModifiers_Pair modpair, int neighborCnt) {
		int sigma = config.cost.costs[origSeqs[a][i-1]][origSeqs[b][j-1]];
		float mod = modpair.subsAB[i][j] + c_weight * modpair.subs[i][j];
		return Math.round( sigma * (1 + normalizer * mod));
	}
	
	protected int calcVLambda(int i, int j, ConsistencyModifiers_Pair modpair, int neighborCnt) {
		int v_ext = config.lambda;
		if(j==0) v_ext = config.leftLambdaTerm();
		if(j==N) v_ext = config.rightLambdaTerm();
		float mod = modpair.vLambdasAB[i][j] + c_weight * modpair.vLambdas[i][j];
		return Math.round( v_ext * (1 + normalizer * mod));
	}
	
	protected int calcVGammaOpen(int i, int j, ConsistencyModifiers_Pair modpair, int neighborCnt){
		int v_open = config.gamma/2;
		if(j==0&&i==1) v_open = config.leftGammaTerm()/2;
		if(j==N) v_open = config.rightGammaTerm()/2;
		float mod = modpair.vGammaOpensAB[i][j] + c_weight * modpair.vGammaOpens[i][j];
		return Math.round( v_open * (1 + normalizer * mod));
	}
	
	protected int calcVGammaClose(int i, int j, ConsistencyModifiers_Pair modpair, int neighborCnt){
		int v_close = config.gamma/2;
		if(j==0) v_close = config.leftGammaTerm()/2;
		if(j==N&&i==M) v_close = config.rightGammaTerm()/2;
		float mod = modpair.vGammaClosesAB[i][j] + c_weight * modpair.vGammaCloses[i][j];
		return Math.round( v_close * (1 + normalizer * mod));
	}
	
	protected int calcHLambda(int i, int j, ConsistencyModifiers_Pair modpair, int neighborCnt){
		int h_ext = config.lambda;
		if(i==0) h_ext = config.leftLambdaTerm();
		if(i==M) h_ext = config.rightLambdaTerm();
		float mod = modpair.hLambdasAB[i][j] + c_weight * modpair.hLambdas[i][j];
		return Math.round( h_ext * (1 + normalizer * mod));
	}
	
	protected int calcHGammaOpen(int i, int j, ConsistencyModifiers_Pair modpair, int neighborCnt){
		int h_open = config.gamma/2;
		if(i==0&&j==1) h_open = config.leftGammaTerm()/2;
		if(i==M) h_open = config.rightGammaTerm()/2;
		float mod = modpair.hGammaOpensAB[i][j] + c_weight * modpair.hGammaOpens[i][j];
		return Math.round( h_open * (1 + normalizer * mod));
	}
	
	protected int calcHGammaClose(int i, int j, ConsistencyModifiers_Pair modpair, int neighborCnt){
		int h_close = config.gamma/2;
		if(i==0) h_close = config.leftGammaTerm()/2;
		if(j==N&&i==M) h_close = config.rightGammaTerm()/2;
		float mod = modpair.hGammaClosesAB[i][j] + c_weight * modpair.hGammaCloses[i][j];
		return Math.round( h_close * (1 + normalizer * mod));
	}
	
	
}
