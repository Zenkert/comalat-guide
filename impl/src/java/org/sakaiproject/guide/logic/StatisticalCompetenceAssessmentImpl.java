package org.sakaiproject.guide.logic;

/**
 * Created by george on 11/8/2016.
 */

import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.guide.model.TestDifficulty;

import java.util.ArrayList;

public class StatisticalCompetenceAssessmentImpl implements StatisticalCompetenceAssessment {

    @Getter
    @Setter
    private ProjectLogic projectLogic;


    @Getter
    @Setter
    private SakaiProxy sakaiProxy;

    @Override
    public double fuzzyGrade(ArrayList<Double> weightsNormalized, ArrayList<Double> percentList, TestDifficulty difficulty) {
        try {


            String fuzzyGradingCode =
                    "FuzzyGrade<-function (delta=0,\n" +
                            "                          a, b, c, d, f,\n" +
                            "                          score_vec = c(0.9,0.8,0.7,1),\n" +
                            "                          w=c(1))\n" +
                            "  {\n" +
                            "\n" +
                            "\n" +
                            "#ideal percentages\n" +
                            "iprg <- c(a,b,c,d,f)\n" +
                            "\n" +
                            "### Define the number of learners\n" +
                            "\n" +
                            "m <- length(score_vec) #number of tests\n" +
                            "\n" +
                            "## membership functions and expected values\n" +
                            "## xEµ [0,1]\n" +
                            "\n" +
                            "## for a\n" +
                            "mf_a<-function(x,iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if((2*a)<=b){\n" +
                            "            if(x>=0 & x<=(1-(2*a))){\n" +
                            "                  mf_a<-0\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_a<-1+((x-1)/((2*a)+delta))\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_a)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            if(x>=0 & x<=1-a-(b/2)){\n" +
                            "                  mf_a<-0\n" +
                            "            }\n" +
                            "            else if (x>(1-a-(b/2)) & x<=(1-a+(b/2))){\n" +
                            "                  mf_a<-1+((x-(1-a+(b/2)))/(b+delta))\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_a<-1\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_a)\n" +
                            "      }\n" +
                            "}\n" +
                            "ev_a<-function(iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if(2*a<=b){\n" +
                            "            ev_a<-(3-2*a)/3\n" +
                            "            return (ev_a)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            ev_a<-(24*a-12*a^2-b^2)/((24*a)+delta)\n" +
                            "            return (ev_a)\n" +
                            "      }\n" +
                            "}\n" +
                            "\n" +
                            "## for b\n" +
                            "mf_b<-function(x,iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if(b>=max((2*a),c)){\n" +
                            "            if(x>=0 & x<=(f+d+(c/2))){\n" +
                            "                  mf_b<-0\n" +
                            "            }\n" +
                            "            else if (x>(f+d+(c/2)) & x<=(f+d+(3*c/2))){\n" +
                            "                  mf_b<-1+((x-(f+d+(3*c/2)))/(c+delta))\n" +
                            "            }\n" +
                            "            else if (x>(f+d+(3*c/2)) & x<=(1-(2*a))){\n" +
                            "                  mf_b<-1\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_b<-1-(x-(1-2*a))/(2*(a+delta))\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_b)\n" +
                            "      }\n" +
                            "      else if((2*a)<b & b<c){\n" +
                            "            if(x>=0 & x<=(1-a-(3*b/2))){\n" +
                            "                  mf_b<-0\n" +
                            "            }\n" +
                            "            else if (x>(1-a-(3*b/2)) & x<=(1-a-(b/2))){\n" +
                            "                  mf_b<-1+((x-(1-a-(b/2)))/(b+delta))\n" +
                            "            }\n" +
                            "            else if(x>(1-a-(b/2)) & x<=(1-(2*a))){\n" +
                            "                  mf_b<-1\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_b<-1-((x-(1-(2*a)))/((2*a)+delta))\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_b)\n" +
                            "      }\n" +
                            "      else if(c<b & b<(2*a)){\n" +
                            "            if(x>=0 & x<=(f+d+(c/2))){\n" +
                            "                  mf_b<-0\n" +
                            "            }\n" +
                            "            else if (x>(f+d+(c/2)) & x<=(f+d+(3*c/2))){\n" +
                            "                  mf_b<-1+(x-(f+d+(3*c/2)))/(c+delta)\n" +
                            "            }\n" +
                            "            else if (x>(f+d+3*(c/2)) & x<=(f+d+c+(b/2))){\n" +
                            "                  mf_b<-1\n" +
                            "            }\n" +
                            "            else if(x>(f+d+c+(b/2)) & x<=(f+d+c+(3*b/2))){\n" +
                            "                  mf_b<-1-((x-(f+d+c+(b/2)))/(b+delta))\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_b<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_b)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            if(x>=0 & x<=(1-a-(3*b/2))){\n" +
                            "                  mf_b<-0\n" +
                            "            }\n" +
                            "            else if (x>(1-a-(3*b/2)) & x<=(1-a-(b/2))){\n" +
                            "                  mf_b<-1+((x-(1-a-(b/2)))/(b+delta))\n" +
                            "            }\n" +
                            "            else if(x>(1-a-(b/2)) & x<=(1-a+(b/2))){\n" +
                            "                  mf_b<-1-((x-(1-a-(b/2)))/(b+delta))\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_b<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_b)\n" +
                            "      }\n" +
                            "}\n" +
                            "ev_b<-function(iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if(b>=max((2*a),c)){\n" +
                            "            ev_b<-(4*a^2-24*a*b-12*b^2+24*b-c^2)/((24*b)+delta)\n" +
                            "            return (ev_b)\n" +
                            "      }\n" +
                            "      else if((2*a)<b & b<c){\n" +
                            "            ev_b<-(4*a^2-24*a*b-13*b^2+24*b)/((24*b)+delta)\n" +
                            "            return (ev_b)\n" +
                            "      }\n" +
                            "      else if(c<b & b<(2*a)){\n" +
                            "            \n" +
                            "            ev_b<-(13*b^2+24*b*c+24*b*d+24*b*f-c^2)/((24*b)+delta)\n" +
                            "            return (ev_b)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            ev_b<-(2-2*a-b)/2\n" +
                            "            return (ev_b)\n" +
                            "      }\n" +
                            "}\n" +
                            "\n" +
                            "## for c\n" +
                            "mf_c<-function(x,iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if(c>=max(b,d)){\n" +
                            "            if(x>=0 & x<=(f+(d/2))){\n" +
                            "                  mf_c<-0\n" +
                            "            }\n" +
                            "            else if (x>(f+(d/2)) & x<=(f+(3*d/2))){\n" +
                            "                  mf_c<-1+((x-(f+(3*d/2)))/(d+delta))\n" +
                            "            }\n" +
                            "            else if(x>(f+(3*d/2)) & x<=(f+d+c-(b/2))){\n" +
                            "                  mf_c<-1\n" +
                            "            }\n" +
                            "            else if (x>(f+d+c-(b/2)) & x<=(f+d+c+(b/2))){\n" +
                            "                  mf_c<-1-((x-(f+d+c-(b/2)))/(b+delta))\n" +
                            "            }\n" +
                            "            else if (x>(f+d+c+(b/2)) & x<=1){\n" +
                            "                  mf_c<-0\n" +
                            "            }\n" +
                            "            return (mf_c)\n" +
                            "      }\n" +
                            "      else if(b<c & c<d){\n" +
                            "            if(x>=0 & x<=(f+d-(c/2))){\n" +
                            "                  mf_c<-0\n" +
                            "            }\n" +
                            "            else if (x>(f+d-(c/2)) & x<=(f+d+(c/2))){\n" +
                            "                  mf_c<-1+(x-(f+d+(c/2)))/(c+delta)\n" +
                            "            }\n" +
                            "            else if(x>(f+d+(c/2)) & x<=(f+d+c-(b/2))){\n" +
                            "                  mf_c<-1\n" +
                            "            }\n" +
                            "            else if(x>(f+d+c-(b/2)) & x<=(f+d+c+(b/2))){\n" +
                            "                  mf_c<-1-(x-(f+d+c-(b/2)))/(b+delta)\n" +
                            "            }\n" +
                            "            else if (x>(f+d+c+(b/2)) & x<=1){\n" +
                            "                  mf_c<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_c)\n" +
                            "      }\n" +
                            "      else if(d<c & c<b){\n" +
                            "            if(x>=0 & x<=(f+(d/2))){\n" +
                            "                  mf_c<-0\n" +
                            "            }\n" +
                            "            else if (x>(f+(d/2)) & x<=(f+(3*d/2))){\n" +
                            "                  mf_c<-1+((x-(f+(3*d/2)))/(d+delta))\n" +
                            "            }\n" +
                            "            else if (x>(f+(3*d/2)) & x<=(f+d+(c/2))){\n" +
                            "                  mf_c<-1\n" +
                            "            }\n" +
                            "            else if(x>(f+d+(c/2)) & x<=(f+d+(3*c/2))){\n" +
                            "                  mf_c<-1-((x-(f+d+(c/2)))/(c+delta))\n" +
                            "            }\n" +
                            "            else if (x>(f+d+(3*c/2)) & x<=1){\n" +
                            "                  mf_c<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_c)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            if(x>=0 & x<=(f+d-(c/2))){\n" +
                            "                  mf_c<-0\n" +
                            "            }\n" +
                            "            else if (x>(f+d-(c/2)) & x<=(f+d+(c/2))){\n" +
                            "                  mf_c<-1+((x-(f+d+(c/2)))/(c+delta))\n" +
                            "            }\n" +
                            "            else if(x>(f+d+c/2) & x<=(f+d+3*(c/2))){\n" +
                            "                  mf_c<-1-((x-(f+d+(c/2)))/(c+delta))\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_c<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_c)\n" +
                            "      }\n" +
                            "}\n" +
                            "ev_c<-function(iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if(c>=max(b,d)){\n" +
                            "            ev_c<-(12*c^2+24*c*d+24*c*f+b^2-d^2)/((24*c)+delta)\n" +
                            "            return (ev_c)\n" +
                            "      }\n" +
                            "      else if(b<c & c<d){\n" +
                            "            ev_c<-(11*c^2+24*c*d+24*c*f+b^2)/((24*c)+delta)\n" +
                            "            return (ev_c)\n" +
                            "      }\n" +
                            "      else if(d<c & c<b){\n" +
                            "        ### error in denominator ev_c<-(13*c^2+24*c*d+24*c*f-d^2)/(24*(b+delta))\n" +
                            "            ev_c<-(13*c^2+24*c*d+24*c*f-d^2)/((24*c)+delta)\n" +
                            "            return (ev_c)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            ev_c<-(2*f+2*d+c)/2\n" +
                            "            return (ev_c)\n" +
                            "      }\n" +
                            "}\n" +
                            "\n" +
                            "## for d\n" +
                            "mf_d<-function(x,iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if(d>=max(c,2*f)){\n" +
                            "            if(x>=0 & x<=(2*f)){\n" +
                            "                  mf_d<-1+(x-2*f)/((2*f)+delta)\n" +
                            "            }\n" +
                            "            else if (x>(2*f) & x<=(f+d-(c/2))){\n" +
                            "                  mf_d<-1\n" +
                            "            }\n" +
                            "            else if(x>(f+d-(c/2)) & x<=(f+d+(c/2))){\n" +
                            "                  mf_d<-1-(x-(f+d-(c/2)))/(c+delta)\n" +
                            "            }\n" +
                            "            else {\n" +
                            "                  mf_d<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_d)\n" +
                            "      }\n" +
                            "      else if(c<d & d<(2*f)){\n" +
                            "            if(x>=0 & x<=(f-(d/2))){\n" +
                            "                  mf_d<-0\n" +
                            "            }\n" +
                            "            else if (x>(f-(d/2)) & x<=(f+(d/2))){\n" +
                            "                  mf_d<-1+((x-(f+(d/2)))/(d+delta))\n" +
                            "            }\n" +
                            "            else if(x>(f+(d/2)) & x<=(f+d-(c/2))){\n" +
                            "                  mf_d<-1\n" +
                            "            }\n" +
                            "            else if(x>(f+d-(c/2)) & x<=(f+d+(c/2))){\n" +
                            "                  mf_d<-1-((x-(f+d-(c/2)))/(c+delta))\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_d<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_d)\n" +
                            "      }\n" +
                            "      else if((2*f)<d & d<c){\n" +
                            "            if(x>=0 & x<=2*f){\n" +
                            "                  mf_d<-1+((x-2*f)/((2*f)+delta))\n" +
                            "            }\n" +
                            "            else if (x>(2*f) & x<=(f+(d/2))){\n" +
                            "                  mf_d<-1\n" +
                            "            }\n" +
                            "            else if (x>(f+(d/2)) & x<=(f+(3*d/2))){\n" +
                            "                  mf_d<-1-((x-(f+(d/2)))/(d+delta))\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_d<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_d)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            if(x>=0 & x<=(f-(d/2))){\n" +
                            "                  mf_d<-0\n" +
                            "            }\n" +
                            "            else if (x>(f-(d/2)) & x<=(f+(d/2))){\n" +
                            "                  mf_d<-1+((x-(f+(d/2)))/(d+delta))\n" +
                            "            }\n" +
                            "            else if(x>(f+(d/2)) & x<=(f+(3*d/2))){\n" +
                            "                  mf_d<-1-((x-(f+(d/2)))/(d+delta))\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_d<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_d)\n" +
                            "      }\n" +
                            "}\n" +
                            "ev_d<-function(iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if(d>=max(c,(2*f))){\n" +
                            "            ev_d<-(12*d^2+24*f*d-4*f^2+c^2)/((24*d)+delta)\n" +
                            "            return (ev_d)\n" +
                            "      }\n" +
                            "      else if(c<d & d<(2*f)){\n" +
                            "            ev_d<-(11*d^2+24*d*f+c^2)/((24*d)+delta)\n" +
                            "            return (ev_d)\n" +
                            "      }\n" +
                            "      else if(2*f<d & d<c){\n" +
                            "            ev_d<-(3*d^2+24*d*f-4*f^2)/((24*d)+delta)\n" +
                            "            return (ev_d)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            ev_d<-((2*f)+d)/2\n" +
                            "            return (ev_d)\n" +
                            "      }\n" +
                            "}\n" +
                            "\n" +
                            "## for f\n" +
                            "mf_f<-function(x,iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if((2*f)<=d){\n" +
                            "            if(x>=0 & x<=(2*f)){\n" +
                            "                  mf_f<-1-(x/((2*f)+delta))\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_f<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_f)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            if(x>=0 & x<=(f-(d/2))){\n" +
                            "                  mf_f<-1\n" +
                            "            }\n" +
                            "            else if (x>(f-(d/2)) & x<=(f+(d/2))){\n" +
                            "                  mf_f<-1-((x-(f-(d/2)))/(d+delta))\n" +
                            "            }\n" +
                            "            else{\n" +
                            "                  mf_f<-0\n" +
                            "            }\n" +
                            "            \n" +
                            "            return (mf_f)\n" +
                            "      }\n" +
                            "}\n" +
                            "ev_f<-function(iprg){\n" +
                            "      a<-iprg[1]\n" +
                            "      b<-iprg[2]\n" +
                            "      c<-iprg[3]\n" +
                            "      d<-iprg[4]\n" +
                            "      f<-iprg[5]\n" +
                            "      if((2*f)<=d){\n" +
                            "            ev_f<-(2*f)/3\n" +
                            "            return (ev_f)\n" +
                            "      }\n" +
                            "      else{\n" +
                            "            ev_f<-(d^2+12*f^2)/((24*f)+delta)\n" +
                            "            return (ev_f)\n" +
                            "      }\n" +
                            "}\n" +
                            "\n" +
                            "# scores is a vector containing the row scores of the quizzes in a course\n" +
                            "# there are n score vectors one for each learner\n" +
                            "#scores<-c(x1,x2,x3,x4,x5)\n" +
                            "\n" +
                            "## M (mx5) matix constuction\n" +
                            "M<-function(scores,iprg,m){\n" +
                            "      # N: the number of quizzes in a course\n" +
                            "      \n" +
                            "      M <-matrix(,nrow=m,ncol=5)\n" +
                            "      for(i in 1:m){\n" +
                            "            M[i,1]<-mf_a(scores[i],iprg)\n" +
                            "            M[i,2]<-mf_b(scores[i],iprg)\n" +
                            "            M[i,3]<-mf_c(scores[i],iprg)\n" +
                            "            M[i,4]<-mf_d(scores[i],iprg)\n" +
                            "            M[i,5]<-mf_f(scores[i],iprg)\n" +
                            "      }\n" +
                            "      return (M)\n" +
                            "      \n" +
                            "      \n" +
                            "}\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "# vector of expected values\n" +
                            "ev<-c(ev_a(iprg),ev_b(iprg),ev_c(iprg),ev_d(iprg),ev_f(iprg))\n" +
                            "\n" +
                            "#defuzzify\n" +
                            "t_score<-M(score_vec,iprg,m)%*%ev\n" +
                            "\n" +
                            "final_score<-function(w,t,m){\n" +
                            "  sum<-0\n" +
                            "  for (i in 1:m){\n" +
                            "    sum<-sum+(w[i]*t[i])\n" +
                            "  }\n" +
                            "  return(sum)\n" +
                            "}\n" +
                            "\n" +
                            "#calculating the fuzzy grade\n" +
                            "fuzzy_grade<-final_score(w,t_score,m)\n" +
                            "\n" +
                            "#return the fuzzy grade\n" +
                            "return(fuzzy_grade)\n" +
                            "\n" +
                            "}\n";

            double a=0.15, b=0.35, c=0.20, d=0.20, f=0.10;
            //based on the difficulty level determine the ideal percentages
            if (difficulty==TestDifficulty.VERY_DIFFICULT) {
                a=0.34; b=0.34; c=0.12; d=0.10; f=0.10;
            } else if (difficulty==TestDifficulty.DIFFICULT) {
                a=0.29; b=0.29; c=0.12; d=0.20; f=0.10;
            } else if (difficulty==TestDifficulty.NORMAL) {
                a=0.23; b=0.23; c=0.14; d=0.20; f=0.20;
            } else if (difficulty==TestDifficulty.EASY) {
                a=0.10; b=0.20; c=0.12; d=0.29; f=0.29;
            } else if (difficulty==TestDifficulty.VERY_EASY) {
                a=0.10; b=0.10; c=0.20; d=0.30; f=0.30;
            }

            //default values for the ideal percentages
            double idealA = a;
            double idealB = b;
            double idealC = c;
            double idealD = d;
            double idealF = f;
            double[] weights = new double[weightsNormalized.size()];
            for (int i = 0; i < weights.length; i++) {
                weights[i] = weightsNormalized.get(i);

            }
            double[] marks = new double[percentList.size()];
            for (int i = 0; i < marks.length; i++) {
                marks[i] = percentList.get(i);

            }

            RCaller rcaller = RCaller.create();
            RCode code = RCode.create();
            code.clear();
            code.addDoubleArray("score_table", marks);
            code.addDoubleArray("w", weights);
            code.addDouble("a", idealA);
            code.addDouble("b", idealB);
            code.addDouble("c", idealC);
            code.addDouble("d", idealD);
            code.addDouble("f", idealF);
            code.addRCode(fuzzyGradingCode);
            code.addRCode("r1<-FuzzyGrade(0,a,b,c,d,f,score_table,w)");

            rcaller.setRCode(code);
            rcaller.runAndReturnResult("r1");
            /**
             * if we wanted to see the exact names that are accessible
             * trough rcaller.getParser() we could print the names and
             * also we could examine the contents of these variables
             * through
             */
            //ArrayList<String> names = rcaller.getParser().getNames();

            //for (String s: names) {
            //    System.out.println(s);
            //}
            //System.out.println(rcaller.getParser().getXMLFileAsString());
            double[] d1 = rcaller.getParser().getAsDoubleArray("r1");
            return d1[0];
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public ArchetypesAnalysisResult archetypesClassification(String shortLanguage) {
        //conduct an ArchetypesAnalysis based on G-V-L-R criteria
        String exportImagePath;

        if (System.getProperty("os.name").startsWith("Windows")) {
            exportImagePath = "c:/var/webapp/";
        } else {
            exportImagePath = "/var/webapp/";
        }

        ArchetypesAnalysis a =
                new ArchetypesAnalysis(exportImagePath,
                        ArchetypesAnalysisCategories.G_R_V_L,
                        sakaiProxy.getCurrentUserId(),
                        projectLogic,
                        shortLanguage
                        );
        return a.benchmarkStudents();
    }

    public static void main(String[] args) {
        StatisticalCompetenceAssessment s = new StatisticalCompetenceAssessmentImpl();
        ArrayList<Double> scores = new ArrayList<>();
        scores.add(0.7);
        scores.add(0.8);
        scores.add(0.7);
        scores.add(1.0);

        ArrayList<Double> weights = new ArrayList<>();
        weights.add(0.5);
        weights.add(0.2);
        weights.add(0.2);
        weights.add(0.1);

        double fmark = s.fuzzyGrade(weights,scores, TestDifficulty.UNDEFINED);
        System.out.println(""+fmark);
    }

}