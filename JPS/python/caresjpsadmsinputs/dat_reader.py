import pandas as pd
import re, csv, json, sys
from io import StringIO


    
def convert_dat(filepath):
    with open(filepath) as f:
        content = re.sub(r'[ ]+',',',re.sub(r'[ ]+\n','\n',f.read()))
        f.close()
    content = StringIO(content) 
    data = pd.read_csv(content, delimiter=',')
    pollutants = list(data.columns)[7:]  
    heights = sorted(set(list(data['Z(m)'])))
    num_heights = len(heights)
    num_pollutant = len(pollutants)
    result = []
    for height in heights:
        height_list = []
        # 1. seperate the rows with different heights first
        data_at_x_m = data.loc[data['Z(m)'] == height]
        # 2. seperate each column after 7 and make each of them a list
        list_at_x_m = data_at_x_m.iloc[:,-num_pollutant:]
        for col in list_at_x_m:
            height_list.append(list(list_at_x_m[col].values))
        result.append(height_list)

        
    print(json.dumps({'grid': result, 'numheight': num_heights, 'listofpol': pollutants, 'numpol': num_pollutant}))


if __name__ == "__main__":#test
    filepath = sys.argv[1]
    convert_dat(filepath)