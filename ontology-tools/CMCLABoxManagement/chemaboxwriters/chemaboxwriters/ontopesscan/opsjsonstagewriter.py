from chemaboxwriters.kgoperations.querytemplates import get_species_iri
from chemutils.obabelutils import obConvert
from chemutils.mathutils import getXYZPointsDistance
from compchemparser.helpers.utils import get_xyz_from_parsed_json
from chemaboxwriters.common.randomidgenerator import get_random_id
import json
import numpy as np
import chemaboxwriters.common.commonvars as commonv
from compchemparser.parsers.ccgaussian_parser import GEOM

SCAN_COORDINATE_ATOMS_IRIS='ScanCoordinateAtomsIRIs'
SCAN_COORDINATE_TYPE='ScanCoordinateType'
SCAN_COORDINATE_UNIT='ScanCoordinateUnit'
SCAN_COORDINATE_VALUE='ScanCoordinateValue'
SCAN_POINTS_JOBS='ScanPointsJobs'

def compchem_opsjson_abox_from_string(data, os_iris, os_atoms_iris, oc_atoms_pos, calc_id=""):
    data_out ={}
    data_out[commonv.SPECIES_IRI] = os_iris.split(',')
    data_out[SCAN_COORDINATE_ATOMS_IRIS] = os_atoms_iris.split(',')
    oc_atoms_pos = [int(at_pos)-1 for at_pos in oc_atoms_pos.split(',')]

    ndegrees = len(os_atoms_iris.split(','))
    if ndegrees == 2:
        data_out[SCAN_COORDINATE_TYPE] = 'Distance coordinate'
        data_out[SCAN_COORDINATE_UNIT] = 'Angstrom'
    else:
        data_out[SCAN_COORDINATE_UNIT] = 'Degree'
        if ndegrees == 3:
            data_out[SCAN_COORDINATE_TYPE] = 'Angle coordinate'
        else:
            data_out[SCAN_COORDINATE_TYPE] = 'Dihedral angle coordinate'

    if not calc_id: calc_id = get_random_id()
    data_out[commonv.ENTRY_UUID]= calc_id
    data_out[commonv.ENTRY_IRI]='PotentialEnergySurfaceScan_'+calc_id

    scanCoordinateValue = []
    ontoCompChemJobs = []
    for data_item in data:
        data_item = json.loads(data_item)

        ontoCompChemJobs.append(data_item[commonv.ENTRY_IRI])
        xyz = np.array(data_item[GEOM])

        if ndegrees==2:
            scanAtomsPos = xyz[oc_atoms_pos]
            scanCoordinateValue.append(getXYZPointsDistance(scanAtomsPos[0],scanAtomsPos[1]))

    data_out[SCAN_COORDINATE_VALUE]=scanCoordinateValue
    data_out[SCAN_POINTS_JOBS]=ontoCompChemJobs

    return json.dumps(data_out)