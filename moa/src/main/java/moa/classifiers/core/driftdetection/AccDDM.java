/*
 *    DDM.java
 *    Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *    @author Manuel Baena (mbaena@lcc.uma.es)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */
package moa.classifiers.core.driftdetection;

import com.github.javacliparser.IntOption;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.tasks.TaskMonitor;

/**
 * Drift detection method based in DDM method of Joao Gama SBIA 2004.
 *
 * <p>João Gama, Pedro Medas, Gladys Castillo, Pedro Pereira Rodrigues: Learning
 * with Drift Detection. SBIA 2004: 286-295 </p>
 *
 * @author Manuel Baena (mbaena@lcc.uma.es)
 * @version $Revision: 7 $
 */
public class AccDDM extends AbstractOptionHandler implements DriftEnsembleDetectionMethod {

    private static final long serialVersionUID = -3518369648142099719L;

    public IntOption minNumInstancesOption = new IntOption(
            "minNumInstances",
            'n',
            "The minimum number of instances before permitting detecting change.",
            30, 0, Integer.MAX_VALUE);

    private int m_n;

    private double m_p[];

    private double m_s[];

    private double m_psmin[];

    private double m_pmin[];

    private double m_smin[];

    private int detNumber = 0;

    public AccDDM() {

    }

    private void initialize() {
//        m_n = new int[detNumber];
        m_n = 1;
        m_p = new double[detNumber];
        m_s = new double[detNumber];
        m_psmin = new double[detNumber];
        m_pmin = new double[detNumber];
        m_smin = new double[detNumber];
        for (int i = 0; i < detNumber; i++) {
//            m_n[i] = 1;
            m_p[i] = 1;
            m_s[i] = 0;
            m_psmin[i] = Double.MAX_VALUE;
            m_pmin[i] = Double.MAX_VALUE;
            m_smin[i] = Double.MAX_VALUE;
        }
    }

    @Override
    public int computeNextVal(boolean[] predictions) {
        if (detNumber == 0) {
            detNumber = predictions.length;
            initialize();
        }
        m_n++;
        for (int i = 0; i < detNumber; i++) {
            if (predictions[i] == false) {
                m_p[i] = m_p[i] + (1.0 - m_p[i]) / (double) m_n;
            } else {
                m_p[i] = m_p[i] - (m_p[i]) / (double) m_n;
            }
            m_s[i] = Math.sqrt(m_p[i] * (1 - m_p[i]) / (double) m_n);


            if (m_n < minNumInstancesOption.getValue()) {
                return DDM_INCONTROL_LEVEL;
            }

            if (m_p[i] + m_s[i] <= m_psmin[i]) {
                m_pmin[i] = m_p[i];
                m_smin[i] = m_s[i];
                m_psmin[i] = m_p[i] + m_s[i];
            }
        }
        
        int am_n = m_n;
        double am_p = average(m_p);
        double am_s = average(m_s);
        double am_smin = average(m_smin);
        double am_pmin = average(m_pmin);

        if (am_n > minNumInstancesOption.getValue() && am_p + am_s > am_pmin + 3 * am_smin) {
            System.out.println(am_p + ",D");
            initialize();
            return DDM_OUTCONTROL_LEVEL;
        } else if (am_p + am_s > am_pmin + 2 * am_smin) {
            System.out.println(am_p + ",W");
            return DDM_WARNING_LEVEL;
        } else {
            System.out.println(am_p + ",N");
            return DDM_INCONTROL_LEVEL;
        }
    }

    private double average(double[] numbers) {
        double sum = 0;
        for (double i : numbers) {
            sum += i;
        }
        return sum / numbers.length;
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
            ObjectRepository repository) {
        // TODO Auto-generated method stub
    }

    @Override
    public DriftEnsembleDetectionMethod copy() {
        return (DriftEnsembleDetectionMethod) super.copy();
    }
}