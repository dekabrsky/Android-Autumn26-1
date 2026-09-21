package Denisova1dz

import Denisova1dz.model.Player //импорт моего класса Player
import org.jfree.chart.ChartFactory //из библиотеки JFreeChart, умеет штамповать готовые графики одной строчкой
import org.jfree.chart.ChartPanel //берет обертку, которая превращает рисунок графика в компонент, который можно вставить в окно
import org.jfree.chart.JFreeChart // тип данных График JFreeChart
import org.jfree.data.general.DefaultPieDataset //импорт данных для круговой диаграммы
import javax.swing.JFrame // для всплывающего графического окна на рабочем столе
import javax.swing.WindowConstants //настройки для окна
import org.jfree.data.category.DefaultCategoryDataset //импортирт данных для столбчатой диаграммы
import org.jfree.chart.plot.PlotOrientation //график (вертикальные столбцы или горизонтальные полосы)

object Task4Charts {

    // Круговая диаграмма долей игроков по странам
    fun showBasicChart(players: List<Player>) { //принимает список игроков players: List<Player> и строит график

        val dataset = DefaultPieDataset<String>() //DefaultPieDataset — контейнер-таблица для круговой диаграммы

        // группируем игроков по странам и считаем их количество
        players
            .groupBy { it.country }       //  <Страна, Список игроков>
            .mapValues { it.value.size }  //  <Страна, Количество игроков>
            .forEach { (country, count) ->
                dataset.setValue(country, count) // берем каждую пару (Страна, Количество) и записываем её в контейнер
            }

        // создаем сам график JFreeChart
        val chart: JFreeChart = ChartFactory.createPieChart(
            "Распределение игроков по странам", // заголовок диаграммы
            dataset, // набор данных
            true, // показывать легенду
            false, //  не показывать всплывающие подсказки
            false //URL-ссылок нет
        )
        displayChart("Базовая задача — Доли стран", chart) //передаем в неё заголовок окна исобранный график
    }

    //Накопленная доля стоимости Топ-10 команд
    fun showAdvancedChart(players: List<Player>) {

        val totalMarketValue = players.sumOf { it.transfer }.toDouble() //складываем общую трансферную стоимость по всему списку игроков, переводим в число с плавающей точкой

        val top10Teams = players
            .groupBy { it.team.name } // группировка по названию команды
            .mapValues { teamEntry -> teamEntry.value.sumOf { player -> player.transfer }.toDouble() } // сумма трансфера всех игроков внутри команды
            .entries //словарь превращаем в список пар, чтобы отсортировать
            .sortedByDescending { it.value } // сортировка команды по убыванию их стоимости
            .take(10) // Берём топ-10 команд

        val teamValues = top10Teams.map { it.value } //отбрасываем название команд, оставляем только список чисел
        val cumulativeValues = teamValues
            .scan(0.0) { accumulatedSum, currentTeamValue -> accumulatedSum + currentTeamValue } //scan складывает по очереди все числа и сохраняет их в новый список
            .drop(1) // Убираем начальный ноль, который добавляет scan

        val dataset = DefaultCategoryDataset() // пустой контейнер для столбчатой диаграммы

        top10Teams.zip(cumulativeValues).forEach { (teamEntry, cumulativeSum) ->
            val teamName = teamEntry.key  // связываем названия команд с накопленными значениями для построения столбцов
            // переводим накопленную сумму в процент
            val cumulativePercent = (cumulativeSum / totalMarketValue) * 100

            // добавляем значение в датасет: (Значение %, Серия, Название команды)
            dataset.addValue(cumulativePercent, "Накопленная доля (%)", teamName)
        }

        // создаем столбчатую диаграмму
        val chart: JFreeChart = ChartFactory.createBarChart(
            "Топ-10 команд: накопленная доля общей стоимости", // Заголовок
            "Команда",  // Подпись оси X
            "Накопленная доля рынка (%)", // Подпись оси Y
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        displayChart("Продвинутая задача — Накопление стоимости Топ-10", chart)
    }

    // Вспомогательный метод для открытия окна с графиком
    private fun displayChart(title: String, chart: JFreeChart) { //функция, принимающая заголовок окна (title) и готовый график (chart)
        val frame = JFrame(title) //создаёт новое графическое окно с указанным заголовком в шапке
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE //при нажатии на крестик закрывает только конкретное окно
        frame.add(ChartPanel(chart)) // рисунок графика в интерактивной панели
        frame.pack() //подгон размера окна под размер графика
        frame.setLocationRelativeTo(null) //выравнивание окна по центру экрана
        frame.isVisible = true //делает окно видимым на экране
    }
}
