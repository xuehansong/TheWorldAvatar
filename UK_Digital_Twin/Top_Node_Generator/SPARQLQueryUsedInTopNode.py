##########################################
# Author: Wanni Xie (wx243@cam.ac.uk)    #
# Last Update Date: 24 June 2021         #
##########################################

"""This module lists out the SPARQL queries used in generating the UK top node graph"""

import os
import json
from rdflib.graph import ConjunctiveGraph
from rdflib.store import NO_STORE
import sys
BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, BASE)
from UK_Digital_Twin_Package.queryInterface import performQuery, performUpdate

qres_pp = []
qres_ec = []
qres_egen = []
qres_ebus = []
qres_eline = []

# query the power plant iri
def queryPowerPlantNodeURL(remoteEndPoint, SleepycatPath, localQuery):
    queryStr = """
    PREFIX powerplant:<http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#> 
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
    SELECT DISTINCT ?powerPlantIRI 
    WHERE { ?powerPlantIRI rdf:type powerplant:PowerPlant .}
    """
    global qres_pp
    if localQuery == False and remoteEndPoint != None:   
        res = json.loads(performQuery(remoteEndPoint, queryStr))
        qres_ = []
        for r in res:
            qres_.append(r["powerPlantIRI"])
        return qres_
    elif SleepycatPath != None and localQuery == True:     
        dt_cg = ConjunctiveGraph('Sleepycat')
        sl = dt_cg.open(SleepycatPath, create = False)
        if sl == NO_STORE:
            print('Cannot find the UK power plant sleepycat store')
            return None
        qres_pp = list(dt_cg.query(queryStr))
        dt_cg.close()
        return qres_pp

#query the location iri in energy consumption graph
def queryUKEnergyConsumptionNodeURL(remoteEndPoint, SleepycatPath, localQuery):
    queryStr = """    
    PREFIX ontoeip_system_function: <http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_function.owl#>
    PREFIX ontocape_upper_level_system: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
    SELECT DISTINCT ?place  
    WHERE 
    {
    ?place rdf:type ontocape_upper_level_system:ExclusiveSubsystem .
    ?place ontoeip_system_function:consumes ?Total_electricity_consumption .    
    }
    """
    global qres_ec
    if remoteEndPoint != None and localQuery == False:  
        res = json.loads(performQuery(remoteEndPoint, queryStr))
        qres_ = []
        for r in res:
            qres_.append(r["place"])
        return qres_    
    elif SleepycatPath != None and localQuery == True:
        dt_cg = ConjunctiveGraph('Sleepycat')
        sl = dt_cg.open(SleepycatPath, create = False)
        if sl == NO_STORE:
            print('Cannot find the UK Energy Consumption sleepycat store')
            return None
        qres_ec = list(dt_cg.query(queryStr))
        dt_cg.close()
        return qres_ec

# query Model_EGen iri
def queryEGenNodeURL(remoteEndPoint, SleepycatPath, localQuery):
    queryStr = """
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
    PREFIX ontopowsys_PowerSystemModel:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#>     
    PREFIX ontocape_mathematical_model: <http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#>
    SELECT DISTINCT ?Model_EGen  
    WHERE 
    {
    ?Model_EGen rdf:type ontopowsys_PowerSystemModel:PowerFlowModelAgent .
    ?Model_EGen ontocape_mathematical_model:hasModelVariable/rdf:type ontopowsys_PowerSystemModel:StartCost .
    } 
    """
    global qres_egen
    if remoteEndPoint != None and localQuery == False: 
        res = json.loads(performQuery(remoteEndPoint, queryStr))
        qres_ = []
        for r in res:
            qres_.append(r["Model_EGen"])
        return qres_    
    elif SleepycatPath != None and localQuery == True:
        dt_cg = ConjunctiveGraph('Sleepycat')
        sl = dt_cg.open(SleepycatPath, create = False)
        if sl == NO_STORE:
            print('Cannot find the Model_EGen sleepycat store')
            return None
        qres_egen = list(dt_cg.query(queryStr))
        dt_cg.close()
        return qres_egen

# query Model_EBus iri
def queryEBusNodeURL(remoteEndPoint, SleepycatPath, localQuery):
    queryStr = """
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
    PREFIX ontopowsys_PowerSystemModel:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#>     
    PREFIX ontocape_mathematical_model: <http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#>
    SELECT DISTINCT ?Model_EBus  
    WHERE 
    {
    ?Model_EBus rdf:type ontopowsys_PowerSystemModel:PowerFlowModelAgent .
    ?Model_EBus ontocape_mathematical_model:hasModelVariable/rdf:type ontopowsys_PowerSystemModel:PdBus . 
    } 
    """
    global qres_ebus
    if remoteEndPoint != None and localQuery == False: 
        res = json.loads(performQuery(remoteEndPoint, queryStr))
        qres_ = []
        for r in res:
            qres_.append(r["Model_EBus"])
        return qres_
    elif SleepycatPath != None and localQuery == True:
        dt_cg = ConjunctiveGraph('Sleepycat')
        sl = dt_cg.open(SleepycatPath, create = False)
        if sl == NO_STORE:
            print('Cannot find the Model_EBus sleepycat store')
            return None
        qres_ebus = list(dt_cg.query(queryStr))
        dt_cg.close()
        return qres_ebus

# query Model_ELine iri
def queryELineNodeURL(remoteEndPoint, SleepycatPath, localQuery):
    queryStr = """
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
    PREFIX ontopowsys_PowerSystemModel:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#>     
    PREFIX ontocape_mathematical_model: <http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#>
    SELECT DISTINCT ?Model_ELine  
    WHERE 
    {
    ?Model_ELine rdf:type ontopowsys_PowerSystemModel:PowerFlowModelAgent .
    ?Model_ELine ontocape_mathematical_model:hasModelVariable/rdf:type ontopowsys_PowerSystemModel:R . 
    } 
    """
    global qres_eline
    if remoteEndPoint != None and localQuery == False: 
        res = json.loads(performQuery(remoteEndPoint, queryStr))
        qres_ = []
        for r in res:
            qres_.append(r["Model_ELine"])
        return qres_
    elif SleepycatPath != None and localQuery == True:
        dt_cg = ConjunctiveGraph('Sleepycat')
        sl = dt_cg.open(SleepycatPath, create = False)
        if sl == NO_STORE:
            print('Cannot find the Model_EBus sleepycat store')
            return None
        qres_eline = list(dt_cg.query(queryStr))
        dt_cg.close()
        return qres_eline


if __name__ == '__main__': 
    sl_path_topo = "C:\\Users\\wx243\\Desktop\\KGB\\My project\\1 Ongoing\\4 UK Digital Twin\\A_Box\\UK_Energy_Consumption\\Sleepycat_UKec_UKtopo"
    sl_path_pp = "C:\\Users\\wx243\\Desktop\\KGB\\My project\\1 Ongoing\\4 UK Digital Twin\\A_Box\\UK_Power_Plant\\Sleepycat_UKpp"   
    res = queryPowerPlantNodeURL('ukpowerplantkg', sl_path_pp, False)
    print (len(res))
    print (res[0], res[1])


