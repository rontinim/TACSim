import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import os
from tqdm import tqdm
class CSVTaskComparator:
    SAVE_PATH = 'D:/tesi/Grafici'

    def __init__(self, folder_path):
        self.folder_path = folder_path
        self.data = pd.DataFrame()
        if not os.path.exists(self.SAVE_PATH):
            os.makedirs(self.SAVE_PATH)

    def read_and_process_data(self):
        files = [file for file in os.listdir(self.folder_path) if file.endswith('.csv')]
        for file in tqdm(files, desc="Loading CSV files"):
            parts = file.split('-')
            city = parts[0]
            num_tasks = int(parts[1])
            num_participants = int(parts[2])
            cache_type = parts[4]
            repetition = int(parts[5].split('.')[0])

            temp_df = pd.read_csv(os.path.join(self.folder_path, file), delimiter=';')
            temp_df['City'] = city
            temp_df['Cache_Type'] = cache_type
            temp_df['Num_Tasks'] = num_tasks
            temp_df['Num_Participants'] = num_participants
            temp_df['Repetition'] = repetition

            if not temp_df.empty:
                self.data = pd.concat([self.data, temp_df], ignore_index=True)
                
        # Map the cache types to their new names
        self.data['Cache_Type'] = self.data['Cache_Type'].map({
            'cache1': 'Cache precisa',
            'cache2': 'Cache approssimata',
            'cache4': 'Algoritmo di Dijkstra'
        })

    def aggregate_data(self):
        self.data['processTime(ms)'] = self.data['processTime(ms)'].astype(float)
        self.aggregated_data_city = self.data.groupby(['City', 'Cache_Type']).agg({
            'processTime(ms)': 'sum'
        }).reset_index()
        self.aggregated_data_tasks = self.data.groupby(['Num_Tasks', 'Cache_Type']).agg({
            'processTime(ms)': 'sum'
        }).reset_index()
        self.aggregated_data_participants = self.data.groupby(['Num_Participants', 'Cache_Type']).agg({
            'processTime(ms)': 'sum'
        }).reset_index()
        self.aggregated_data_repetition = self.data.groupby(['Repetition', 'Cache_Type']).agg({
            'processTime(ms)': 'sum'
        }).reset_index()

    def plot_city_comparison(self):
        # Ensure the Cache_Type order
        cache_type_order = ['Cache precisa', 'Cache approssimata', 'Algoritmo di Dijkstra']
        self.aggregated_data_city['Cache_Type'] = pd.Categorical(self.aggregated_data_city['Cache_Type'], categories=cache_type_order, ordered=True)
        
        # Sort cities based on 'Algoritmo di Dijkstra' process time
        sorted_cities = self.aggregated_data_city[self.aggregated_data_city['Cache_Type'] == 'Algoritmo di Dijkstra'].sort_values(by='processTime(ms)')['City']
        sorted_data_city = self.aggregated_data_city[self.aggregated_data_city['City'].isin(sorted_cities)]
        sorted_data_city['City'] = pd.Categorical(sorted_data_city['City'], categories=sorted_cities, ordered=True)
        
        plt.figure(figsize=(14, 10))
        sns.barplot(data=sorted_data_city, x='City', y='processTime(ms)', hue='Cache_Type', hue_order=cache_type_order, ci=None)
        plt.title('Confronto dei Tempi di Calcolo Totali tra Tipologie di Cache per Città')
        plt.xlabel('Città')
        plt.ylabel('Tempo Totale di Calcolo (ms)')
        plt.legend(title='Tipologia di Cache')
        plt.grid(True)
        save_path = os.path.join(self.SAVE_PATH, 'city_comparison.png')
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        plt.show()

    def plot_tasks_comparison(self):
        cache_type_order = ['Cache precisa', 'Cache approssimata', 'Algoritmo di Dijkstra']
        plt.figure(figsize=(14, 10))
        sns.barplot(data=self.aggregated_data_tasks, x='Num_Tasks', y='processTime(ms)', hue='Cache_Type', hue_order=cache_type_order, ci=None)
        plt.title('Confronto dei Tempi di Calcolo Totali tra Tipologie di Cache per Numero di Task')
        plt.xlabel('Numero di Task')
        plt.ylabel('Tempo Totale di Calcolo (ms)')
        plt.legend(title='Tipologia di Cache')
        plt.grid(True)
        save_path = os.path.join(self.SAVE_PATH, 'tasks_comparison.png')
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        plt.show()

    def plot_participants_comparison(self):
        cache_type_order = ['Cache precisa', 'Cache approssimata', 'Algoritmo di Dijkstra']
        plt.figure(figsize=(14, 10))
        sns.barplot(data=self.aggregated_data_participants, x='Num_Participants', y='processTime(ms)', hue='Cache_Type', hue_order=cache_type_order, ci=None)
        plt.title('Confronto dei Tempi di Calcolo Totali tra Tipologie di Cache per Numero di Partecipanti')
        plt.xlabel('Numero di Partecipanti')
        plt.ylabel('Tempo Totale di Calcolo (ms)')
        plt.legend(title='Tipologia di Cache')
        plt.grid(True)
        save_path = os.path.join(self.SAVE_PATH, 'participants_comparison.png')
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        plt.show()

    def plot_repetition_comparison(self):
        cache_type_order = ['Cache precisa', 'Cache approssimata', 'Algoritmo di Dijkstra']
        plt.figure(figsize=(14, 10))
        sns.lineplot(data=self.aggregated_data_repetition, x='Repetition', y='processTime(ms)', hue='Cache_Type', hue_order=cache_type_order, marker='o')
        plt.title('Confronto dei Tempi di Calcolo Totali tra Tipologie di Cache per Ripetizione')
        plt.xlabel('Ripetizione')
        plt.ylabel('Tempo Totale di Calcolo (ms)')
        plt.legend(title='Tipologia di Cache')
        plt.grid(True)
        save_path = os.path.join(self.SAVE_PATH, 'repetition_comparison.png')
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        plt.show()

    def plot_distance_comparison(self):
        cache_precisa_data = self.data[self.data['Cache_Type'] == 'Cache precisa']
        cache_approssimata_data = self.data[self.data['Cache_Type'] == 'Cache approssimata']
        cache_approssimata_keys = set(
            zip(cache_approssimata_data['startLatitude'], cache_approssimata_data['startLongitude'], cache_approssimata_data['endLatitude'], cache_approssimata_data['endLongitude'])
        )
        cache_precisa_data_filtered = cache_precisa_data[
            cache_precisa_data.apply(
                lambda row: (row['startLatitude'], row['startLongitude'], row['endLatitude'], row['endLongitude']) in cache_approssimata_keys,
                axis=1
            )
        ]
        cache_precisa_aggregated = cache_precisa_data_filtered.groupby('City')['distance(km)'].sum().reset_index()
        cache_approssimata_aggregated = cache_approssimata_data.groupby('City')['distance(km)'].sum().reset_index()
        distance_comparison = pd.merge(
            cache_precisa_aggregated, cache_approssimata_aggregated, on='City', suffixes=('_Cache_precisa', '_Cache_approssimata')
        )
        
        # Calcolare la percentuale di aumento della distanza per Cache approssimata rispetto a Cache precisa
        distance_comparison['percentage_increase'] = ((distance_comparison['distance(km)_Cache_approssimata'] - distance_comparison['distance(km)_Cache_precisa']) / distance_comparison['distance(km)_Cache_precisa']) * 100
        
        # Creare il grafico
        plt.figure(figsize=(14, 10))
        sns.barplot(data=distance_comparison, x='City', y='percentage_increase', ci=None)
        plt.title('Percentuale di Aumento della Distanza con Cache approssimata rispetto a Cache precisa per Città')
        plt.xlabel('Città')
        plt.ylabel('Percentuale di Aumento della Distanza (%)')
        plt.grid(True)
        save_path = os.path.join(self.SAVE_PATH, 'distance_comparison.png')
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        plt.show()

if __name__ == "__main__":
    folder_path = 'D:/tacSim/TACSim/csvSimulazioniSPERIMENT1'
    comparator = CSVTaskComparator(folder_path)
    comparator.read_and_process_data()
    comparator.aggregate_data()
    
    comparator.plot_city_comparison()
    comparator.plot_tasks_comparison()
    comparator.plot_participants_comparison()
    comparator.plot_repetition_comparison()
    comparator.plot_distance_comparison()
