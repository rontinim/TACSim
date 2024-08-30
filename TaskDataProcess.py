import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import os

class TasksDataProcessor:
    def __init__(self, folder_path):
        self.folder_path = folder_path
        self.cache_data = pd.DataFrame()  
        self.raw_data = pd.DataFrame()    
        self.ram_data = pd.DataFrame()
        self.task_cache_data = pd.DataFrame()  
        self.all_tasks_data = pd.DataFrame() 
        self.city_task_cache_data = pd.DataFrame() 

    def read_and_process_data(self):
        combined_data = pd.DataFrame()
        for file in os.listdir(self.folder_path):
            if file.endswith('.csv'):
                parts = file.split('-')
                city = parts[0]
                num_tasks = int(parts[1])  # Estrai il numero di task correttamente

                temp_df = pd.read_csv(os.path.join(self.folder_path, file))
                num_rows = len(temp_df)  # Conta le righe nel file CSV

                row_data = pd.DataFrame({
                    'City': [city],
                    'Num_Tasks': [num_tasks],
                    'Num_Rows': [num_rows]
                })

                combined_data = pd.concat([combined_data, row_data], ignore_index=True)

        # Raggruppare e calcolare la media delle righe per città e numero di task
        self.grouped_data = combined_data.groupby(['City', 'Num_Tasks']).agg({'Num_Rows': 'mean'}).reset_index()


    def plot_data(self):
        plt.figure(figsize=(14, 10))
        sns.lineplot(data=self.grouped_data, x='Num_Tasks', y='Num_Rows', hue='City', marker='o', linestyle='-')
        plt.title('Media del numero di righe calcolate nei CSV per Città')
        plt.xlabel('Numero di Task')
        plt.ylabel('Media delle righe')
        plt.legend(title='Città')
        plt.grid(True)
        plt.show()


    def read_text_data(self, filename):
        filepath = os.path.join(self.folder_path, filename)
        data = []
        with open(filepath, 'r', encoding='utf-8') as file:
            for line in file:
                line = line.strip()
                if line.startswith('Città:'):
                    parts = line.split(',')
                    city = parts[0].split(':')[1].strip()
                    tasks = int(parts[1].split(':')[1].strip())
                    nodes = int(parts[3].split(':')[1].strip())
                    arcs = int(parts[4].split(':')[1].strip())
                    cache_description = parts[2].strip().split('---')[1].strip()

                    if "Preciso" in cache_description:
                        cache_type = "Precise Cache"
                    elif "Approssimato" in cache_description:
                        cache_type = "Approximate Cache"
                    else:
                        cache_type = "No Cache"
                elif 'Num. Partecipanti:' in line:
                    execution_time = float(line.split('Tempo Scenario:')[1].split('ms')[0].strip())

                    data.append({
                        'City': city,
                        'Tasks': tasks,
                        'ExecutionTime': execution_time,
                        'Nodes': nodes,
                        'Arcs': arcs,
                        'Cache_Type': cache_type
                    })

        self.raw_data = pd.DataFrame(data)

    def aggregate_data_by_task(self):
        self.task_data = self.raw_data.groupby(['Tasks', 'City']).agg({
            'ExecutionTime': 'mean'
        }).reset_index()

    def aggregate_data_by_city_and_task(self):
        self.city_task_data = self.raw_data.groupby(['City', 'Tasks']).agg({
            'ExecutionTime': 'mean'
        }).reset_index()

    def aggregate_data_for_all_tasks(self):
        self.all_tasks_data = self.raw_data.groupby(['City']).agg({
            'ExecutionTime': 'mean'
        }).reset_index()

    def aggregate_data_by_city_task_cache(self):
        self.city_task_cache_data = self.raw_data.groupby(['City', 'Tasks', 'Cache_Type']).agg({
            'ExecutionTime': 'mean'
        }).reset_index()

    def plot_performance_by_city_and_task(self):
        for city in self.city_task_data['City'].unique():
            city_data = self.city_task_data[self.city_task_data['City'] == city]
            city_data = city_data.sort_values(by='ExecutionTime')  # Sorting by execution time
            plt.figure(figsize=(10, 6))
            sns.barplot(x='Tasks', y='ExecutionTime', data=city_data)
            plt.title(f'Tempo di esecuzione con cache per la città {city}')
            plt.xlabel('Numero di Task')
            plt.ylabel('Tempo di esecuzione (s)')
            plt.show()

    def plot_average_performance_across_cities(self):
        if self.all_tasks_data.empty:
            print("No data to display")
            return
        # Ensure data is sorted by execution time
        sorted_data = self.all_tasks_data.sort_values(by='ExecutionTime')
        plt.figure(figsize=(12, 8))
        sns.barplot(x='City', y='ExecutionTime', data=sorted_data)
        plt.title('Tempo Medio di Esecuzione per Città in Ordine Crescente')
        plt.xlabel('Città')
        plt.ylabel('Tempo Medio di Esecuzione (s)')
        plt.show()

    def plot_performance_by_task_across_cities(self):
        unique_tasks = self.task_data['Tasks'].unique()
        for task in sorted(unique_tasks):
            task_specific_data = self.task_data[self.task_data['Tasks'] == task]
            task_specific_data = task_specific_data.sort_values(by='ExecutionTime')  # Sorting by execution time
            plt.figure(figsize=(10, 6))
            sns.barplot(x='City', y='ExecutionTime', data=task_specific_data)
            plt.title(f'Tempo di Esecuzione per {task} Tasks dalla Città')
            plt.xlabel('Città')
            plt.ylabel('Tempo di Esecuzione(s)')

            # Fetch nodes and arcs for legend
            nodes_arcs = {city: (nodes, arcs) for city, nodes, arcs in self.raw_data[['City', 'Nodes', 'Arcs']].drop_duplicates().values}
            legend_labels = [f'{city} - {nodes_arcs[city][0]} nodes, {nodes_arcs[city][1]} arcs' for city in task_specific_data['City']]
            plt.legend(legend_labels, title='City - Nodes/Arches', loc='upper left')
            plt.show()


    def plot_performance_by_task(self):
        task_numbers = [20, 40, 60, 80, 100]
        for tasks in task_numbers:
            task_data = self.text_data[self.text_data['Tasks'] == tasks]
            task_data = task_data.groupby(['City']).agg({
                'ExecutionTime': 'mean',
                'Nodes': 'first',
                'Arcs': 'first'
            }).reset_index()
            task_data.sort_values(by='ExecutionTime', inplace=True)

            plt.figure(figsize=(10, 6))
            bar_plot = sns.barplot(x='City', y='ExecutionTime', data=task_data)
            plt.title(f'Tempo di Esecuzione per {tasks} Tasks la città')
            plt.xlabel('Città')
            plt.ylabel('Tempo di Esecuzione (s)')

            legend_labels = [f'{row["City"]} - {int(row["Nodes"])} nodes, {int(row["Arcs"])} arcs' for index, row in task_data.iterrows()]
            plt.legend(legend_labels, title='City - Nodes/Arches', loc='upper left')
            plt.show()

    def plot_cache_comparison_across_tasks(self):
        cities = self.cache_data['City'].unique()
        for city in cities:
            city_data = self.cache_data[self.cache_data['City'] == city]
            sns.barplot(x='Tasks', y='ExecutionTime', hue='Cache_Type', data=city_data, ci=None)
            plt.title(f'Tempo di Esecuzione per Tipologia di Cache a {city}')
            plt.xlabel('Numero di Task')
            plt.ylabel('Tempo di esecuzione (s)')
            plt.legend(title='Tipo di Cache')
            plt.show()

    def plot_city_task_cache_performance(self):
        for city in self.city_task_cache_data['City'].unique():
            city_data = self.city_task_cache_data[self.city_task_cache_data['City'] == city]
            plt.figure(figsize=(12, 8))
            sns.barplot(x='Tasks', y='ExecutionTime', hue='Cache_Type', data=city_data)
            plt.title(f'Performance Across Tasks and Cache Types for {city}')
            plt.xlabel('Numero di Task')
            plt.ylabel('Tempo di esecuzione (s)')
            plt.legend(title='Tipo di Cache')
            city_details = self.raw_data[self.raw_data['City'] == city].iloc[0]
            plt.annotate(f'Nodes: {city_details["Nodes"]}, Arcs: {city_details["Arcs"]}', (0, 0), (10, -40), xycoords='axes fraction', textcoords='offset points', va='top')
            plt.show()

    def read_ram_data(self, filename):
        filepath = os.path.join(self.folder_path, filename)
        data = []
        with open(filepath, 'r', encoding='utf-8') as file:
            current_city = None
            for line in file:
                line = line.strip()
                if 'Città corrente:' in line:
                    current_city = line.split(': ')[1]
                if 'Max Ram Utilizzata per il calcolo delle distanze:' in line and current_city:
                    max_ram_calcolo = float(line.split('Max Ram Utilizzata per il calcolo delle distanze:')[1].split('Mb')[0].strip())
                    max_ram_download = float(line.split('Max Ram utilizzata per il download della mappa:')[1].split('Mb')[0].strip())
                    data.append({
                        'City': current_city,
                        'MaxRAMCalc': max_ram_calcolo,
                        'MaxRAMDownload': max_ram_download
                    })
        self.ram_data = pd.DataFrame(data)
        self.ram_data = self.ram_data.groupby('City').max().reset_index()

    def plot_ram_usage(self):
        plt.figure(figsize=(12, 8))
        self.ram_data = self.ram_data.sort_values(['MaxRAMCalc', 'MaxRAMDownload'], ascending=True)
        bar_width = 0.35
        r1 = range(len(self.ram_data))
        r2 = [x + bar_width for x in r1]
        plt.bar(r1, self.ram_data['MaxRAMCalc'], color='blue', width=bar_width, edgecolor='grey', label='Max RAM for Distance Calc')
        plt.bar(r2, self.ram_data['MaxRAMDownload'], color='green', width=bar_width, edgecolor='grey', label='Max RAM for Map Download')
        plt.xlabel('City', fontweight='bold')
        plt.xticks([r + bar_width / 2 for r in range(len(self.ram_data))], self.ram_data['City'])
        plt.ylabel('Uso Ram (MB)')
        plt.title('Max RAM Usata per la città')
        plt.legend()
        plt.show()

if __name__ == "__main__":
    folder_path = 'D:/tacSim/TACSim/csvSimulazioni'
    processor = TasksDataProcessor(folder_path)
    processor.read_and_process_data()
    processor.plot_data()
    folder_path_txt = 'D:/tacSim/TACSim'
    processorTxt = TasksDataProcessor(folder_path_txt)
    processorTxt.read_text_data("Sperimentation.txt")
    
    processorTxt.aggregate_data_by_city_and_task()
    processorTxt.plot_performance_by_city_and_task()

    processorTxt.aggregate_data_by_task()
    processorTxt.plot_performance_by_task_across_cities()

    processorTxt.aggregate_data_for_all_tasks()
    processorTxt.plot_average_performance_across_cities()
    processorTxt.aggregate_data_by_city_task_cache()
    processorTxt.plot_city_task_cache_performance()

    processorTxt.read_ram_data("SimulationsEssence.txt")
    processorTxt.plot_ram_usage()
	